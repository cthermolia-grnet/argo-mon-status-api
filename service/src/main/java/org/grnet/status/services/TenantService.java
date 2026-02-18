package org.grnet.status.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.authorizations.groups.GroupManagement;
import org.grnet.status.authorizations.service.AccessControlService;
import org.grnet.status.authorizations.service.AuthGroupSetupService;
import org.grnet.status.dtos.ams.PublishRequest;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.readiness.TenantReadiness;
import org.grnet.status.dtos.readiness.WebApiTenantReadiness;
import org.grnet.status.dtos.tenant.ContactDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.alerts.AlertDefinitionRequest;
import org.grnet.status.dtos.tenant.status.EventStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusFullResponse;
import org.grnet.status.entities.Contact;
import org.grnet.status.entities.Tenant;
import org.grnet.status.enums.*;
import org.grnet.status.exceptions.CustomRuntimeException;
import org.grnet.status.mappers.TenantMapper;
import org.grnet.status.repositories.ContactRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.clients.AmsService;
import org.grnet.status.services.clients.WebApiService;
import org.grnet.status.services.utils.ImageUploadUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@ApplicationScoped

public class TenantService {

    @Inject
    TenantRepository tenantRepository;
    @Inject
    ContactRepository contactRepository;
    @Inject
    AuthGroupSetupService authGroupSetupService;

    @Inject
    AccessControlService accessControlService;

    @Inject
    WebApiService webApiService;

    @Inject
    GroupManagement groupManagement;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String namespace;

    @Inject
    ImageUploadUtil imageUploadUtil;

    @ConfigProperty(name = "base.upload.logo.dir")
    String baseUploadTenantsImagesDir;

    @ConfigProperty(name = "api.server.url")
    String apiServerUrl;

    @Inject
    AmsService amsService;


    private final ExecutorService executorService = Executors.newFixedThreadPool(2); // Adjust as needed

    public TenantResponseDto create(TenantRequestDto request, String userId) throws IOException {

        var response = createTenant(request, userId);

        try {
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("tenantId", List.of(response.id));
            attributes.put("description", List.of(request.info.description));

            var parentPath = "/" + namespace + "/tenants";

            authGroupSetupService.createGroup(parentPath, response.info.name, List.of("admin", "viewer"), attributes);

        } catch (Exception ex) {
            Log.error("Failed to create AGM group for tenant " + response.id + ": " + ex.getMessage());
        }

        return response;
    }

    /**
     * Create a tenant
     *
     * @param request , TenantRequestDto with all the info needed
     * @param userId  , the creator of the tenant
     * @return, TenantResponseDto representing the tenant's info
     */
    @Transactional
    public TenantResponseDto createTenant(TenantRequestDto request, String userId) throws IOException {

        var existTenantOpt = tenantRepository.fetchTenantByName(request.info.name);
        if (existTenantOpt.isPresent()) {
            var message = "Tenant: " + existTenantOpt.get().name + " already exists in ARGO Mon Status API with id: " + existTenantOpt.get().id;
            throw new CustomRuntimeException(409, message, new HashSet<>());
        }
        handleImage(request);

        var tenant = TenantMapper.INSTANCE.dtoToTenant(request.info);
        boolean tenantCreatedRemotely = false;
        String remoteTenantId = null;

        var webApiRequest = TenantMapper.INSTANCE.toWebApiRequest(request);
        var webApiCreateResponse = webApiService.createTenantInWebApi(webApiRequest);
        remoteTenantId = webApiCreateResponse.getData().getId();
        tenantCreatedRemotely = true;
        var status = TenantMapper.INSTANCE.mapStatusToString(setDefaultStatus());
        tenant.setStatus(status);

        try {
            TenantMapper.INSTANCE.mapMetadata(request, tenant);
            writeInDB(request, tenant, remoteTenantId, userId);
            sendNotifications(tenant);
            //   var  tenantWithStatus=tenantRepository.findById(tenant.id);
            return TenantMapper.INSTANCE.tenantToDto(tenant);
        } catch (Exception e) {
            // If tenant was created remotely, but something failed locally, rollback remote creation
            if (tenantCreatedRemotely && remoteTenantId != null) {
                webApiService.deleteTenant(remoteTenantId);
            }
            throw e;
        }
    }

    /**
     * Get a tenant by Id.
     */
    public TenantResponseDto getTenantById(String id) {
        TenantResponseDto webtenant = null;
        try {
            var tenant = tenantRepository.findById(id);
            var webapiGetResponse = webApiService.retrieveTenantWebApi(tenant.id);
            webtenant = TenantMapper.INSTANCE.webApiTenantToDto(tenant, webapiGetResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var tenant = tenantRepository.findById(id);
        if (tenant != null) {
            webtenant.contacts = TenantMapper.INSTANCE.contactsToDtos(tenant.getContacts());
            webtenant.groupStatus = getGroupStatus(tenant);
        }
        return webtenant;
    }

    public void deleteTenantById(String id) {

        var tenant = tenantRepository.findById(id);
        if (tenant == null) {
            throw new WebApplicationException("Tenant not found: " + id, 404);
        }

        var tenantName = tenant.name;
        deleteTenant(tenant.getId());

        try {
            var parentPath = "/" + namespace + "/tenants/";
            var groupPath = parentPath + tenantName;

            authGroupSetupService.deleteGroup(groupPath);

        } catch (Exception ex) {
            Log.error("Failed to queue async AGM group deletion for tenant " + id + ": " + ex.getMessage());
        }
    }

    /**
     * Delete a tenant by Id.
     */
    @Transactional
    public void deleteTenant(String id) {

        var tenant = tenantRepository.findById(id);

        try {
            // Create a copy so we can check which contacts might become orphan
            Set<Contact> oldContacts = new HashSet<>(tenant.getContacts());

            // 1. Remove the relation from both sides
            for (Contact c : oldContacts) {
                c.getTenants().remove(tenant); // remove tenant from contact
            }
            tenant.getContacts().clear(); // remove contacts from tenant
            tenantRepository.persist(tenant); // update join table

            // 2. Now safely delete the tenant
            tenantRepository.delete(tenant);

            // 3. Delete image and external API
            imageUploadUtil.deleteImageIfExists(baseUploadTenantsImagesDir, tenant.name);

            webApiService.deleteTenant(id);

            // 4. Delete orphan contacts
            deleteOrphanContacts(oldContacts);

        } catch (RuntimeException e) {
            int status = 500;

            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }

            throw new WebApplicationException(e.getMessage(), status);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete all tenants.
     */
    @Transactional
    public void deleteAll() {

        var tenants = tenantRepository.fetchTenants();

        tenants.forEach(t -> {
            try {

                var id = t.id;// 1. Delete from DB first (inside transaction)
                Set<Contact> oldContacts = new HashSet<>(t.getContacts());

                tenantRepository.delete(t);
                oldContacts.stream().forEach(c -> {
                    contactRepository.delete(c);
                });

                imageUploadUtil.deleteImageIfExists(baseUploadTenantsImagesDir, t.name);
                webApiService.deleteTenant(id);

            } catch (RuntimeException e) {

                // If DB delete fails -> API delete is NOT executed, as desired

                int status = 500;
                if (e instanceof WebApplicationException) {
                    status = ((WebApplicationException) e).getResponse().getStatus();
                }

                var message = e.getMessage();
                Log.error("ERROR deleting tenant " + t.id + " -> " + status + ": " + message);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Update an existing tenant.
     */
    @Transactional
    public TenantResponseDto updateTenant(String id, @Valid TenantRequestDto request) throws IOException {
        handleImage(request);

        // ------------------------------
        // 1. Get previous remote state (for rollback)
        // ------------------------------

        var previousWebApiTenant = webApiService.retrieveTenantWebApi(id);

        var previousData = previousWebApiTenant.getData().get(0);

        if (request.info != null && request.info.name != null &&
                !Objects.equals(previousData.getInfo().getName(), request.info.name)) {
            throw new WebApplicationException("Tenant name cannot be changed", 409);
        }

        var previousRemoteState = TenantMapper.INSTANCE.webApiTenantToTenantRequestDto(
                previousWebApiTenant.getData().get(0).getInfo()
        );
        // ------------------------------
        // 2. Update remote API first
        // ------------------------------

        // create an initial webApiRequest with the existing web api data for the tenant
        var webApiRequest = TenantMapper.INSTANCE.dataToTenantWebApiRequest(previousWebApiTenant.getData().get(0));
        //update the initial webApiRequest with the new data for info and topology while keeping users and dbConf as it is
        TenantMapper.INSTANCE.updateExistingWebApiRequest(request, webApiRequest);

        //updates the tenant in the webApi
        webApiService.updateTenantWebApi(webApiRequest, id);
        // ------------------------------
        // 3. Local DB update
        // ------------------------------
        var tenant = tenantRepository.findById(id);

        // Keep copy of old contacts for orphan check
        Set<Contact> oldContacts = new HashSet<>(tenant.getContacts());

        try {
            updateTenantInDB(request, tenant);
            // ------------------------------
            // 5. Delete orphan contacts (contact with 0 tenants)
            // ------------------------------
            deleteOrphanContacts(oldContacts);

        } catch (Exception dbException) {

            try {
                var webApiRequestPreviousState = TenantMapper.INSTANCE.toWebApiRequest(previousRemoteState);
                webApiService.updateTenantWebApi(webApiRequestPreviousState, id);
            } catch (Exception rollbackEx) {
                throw new WebApplicationException(
                        "DB update failed AND remote rollback failed: " + rollbackEx.getMessage(),
                        500
                );
            }
            throw new RuntimeException("DB update failed: " + dbException.getMessage());
        }

        return TenantMapper.INSTANCE.tenantToDto(tenant);
    }

    /**
     * Retrieves a page of tenant objects existing.
     *
     * @param page    The index of the page to retrieve (starting from 0).
     * @param size    The maximum number of tenant objects to include in a page.
     * @param uriInfo The Uri Info.
     * @return A list of TenantResponseDto objects representing the submitted tenant objects in the requested page.
     */
    public PageResource<TenantResponseDto> getTenantsByPageAndSize(int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        ArrayList<TenantResponseDto> tenantList = new ArrayList<>();
        var tenants = tenantRepository.fetchTenantsByPageAndSize(page, size, search, sort, order);
        tenants.list().stream().forEach(t -> {
            TenantResponseDto webtenant = null;
            try {
                var webTenantGetResponse = webApiService.retrieveTenantWebApi(t.id);
                webtenant = TenantMapper.INSTANCE.webApiTenantToDto(t, webTenantGetResponse);
                webtenant.groupStatus = getGroupStatus(t);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            tenantList.add(webtenant);
        });
        return new PageResource<>(tenants, tenantList, uriInfo);
    }


    public PageResource<TenantResponseDto> listAuthorizedTenants(int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        if (accessControlService.isSuperAdmin()) {
            return getTenantsByPageAndSize(page, size, uriInfo, search, sort, order);
        }

        var allowedTenantIds = accessControlService.resolveAccessibleGroupsByName("tenants");

        var tenants = tenantRepository.fetchTenantsByIdsAndPageAndSize(allowedTenantIds, page, size, search, sort, order);

        var tenantList = new ArrayList<TenantResponseDto>();

        tenants.list().forEach(t -> {
            TenantResponseDto webtenant = null;
            try {
                var webTenantGetResponse = webApiService.retrieveTenantWebApi(t.id);
                webtenant = TenantMapper.INSTANCE.webApiTenantToDto(t, webTenantGetResponse);
                webtenant.groupStatus = getGroupStatus(t);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            tenantList.add(webtenant);
        });
        return new PageResource<>(tenants, tenantList, uriInfo);
    }

    private void handleImage(TenantRequestDto request) {

        var image = request.info.image;
        if (image != null && image.startsWith("data:image/")) {

            imageUploadUtil.validateBase64Image(image);
            var savedPath = imageUploadUtil.saveBase64Image(baseUploadTenantsImagesDir, image, request.info.name, "/logos/");
            request.info.image = apiServerUrl + savedPath;
        }
    }

    private Set<Contact> resolveAndMergeContacts(TenantRequestDto request) {

        Set<Contact> result = new HashSet<>();

        if (request.contacts != null) {
            for (ContactDto dto : request.contacts) {
                // Try to find existing contact in DB by unique fields
                try {
                    Optional<Contact> existing = contactRepository.fetchContactByInfo(dto.name, dto.email, dto.type);
                    if (existing.isPresent()) {
                        result.add(existing.get());
                    } else {
                        // Create new contact (MapStruct mapping)
                        Contact newContact = new Contact();
                        newContact.setContactName(dto.name);
                        newContact.setContactEmail(dto.email);
                        newContact.setContactType(ContactType.valueOf(dto.type));
                        contactRepository.persist(newContact);
                        result.add(newContact);
                    }
                } catch (RuntimeException e) {
                    throw e; // simply rethrow original exception with full stack trace
                }
            }
        }
        return result;
    }

    private void deleteOrphanContacts(Set<Contact> oldContacts) {
        for (Contact contact : oldContacts) {
            if (contact.getTenants() == null || contact.getTenants().isEmpty()) {
                contactRepository.delete(contact);
            }
        }
    }

    //construct and stores a tenant in the database
    private Tenant writeInDB(TenantRequestDto request, Tenant tenant, String remoteTenantId, String userId) {

        tenant.id = remoteTenantId;
        tenant.updatedBy = userId;
        Set<Contact> contacts = resolveAndMergeContacts(request);

        tenant.setContacts(new HashSet(contacts));
        try {
            tenantRepository.persist(tenant);
            return tenant;
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Rethrow to keep transactional behavior
        }
    }

    //updates the tenant in the database
    private void updateTenantInDB(TenantRequestDto request, Tenant tenant) {
        // Update simple fields:
        TenantMapper.INSTANCE.updateToTenant(request, tenant);
        TenantMapper.INSTANCE.mapMetadata(request, tenant);  // serialize metadata separately

        // ------------------------------
        // 4. Update tenant.contacts
        // ------------------------------
        Set<Contact> updatedContacts = resolveAndMergeContacts(request);
        tenant.setContacts(updatedContacts);
//
//            // Replace tenant.contacts with new set
        tenant.setContacts(updatedContacts);

        TenantMapper.INSTANCE.mapMetadata(request, tenant);
        tenantRepository.persist(tenant);
        tenantRepository.flush(); // force errors
    }

    private void updateTenantStatusInDb(Tenant tenant, String updatedStatusJson) {
        // Update simple fields:

        tenant.setStatus(updatedStatusJson);
        tenantRepository.persist(tenant);
        tenantRepository.flush(); // force errors
    }

    @Transactional
    public TenantStatusFullResponse updateTenantManualJobs(String id, @Valid TenantStatusDto request) {
        validateJobsMode(request.jobs, EventMode.MANUAL);

        if (request.jobs != null) {
            request.jobs.forEach(this::applyJobDefinition);
        }

        return updateTenantJobsInternal(id, request);
    }

    @Transactional
    public TenantStatusFullResponse updateTenantAutoJobs(String id, @Valid TenantStatusDto request) {
        validateJobsMode(request.jobs, EventMode.AUTO);
        if (request.jobs != null) {
            request.jobs.forEach(this::applyJobDefinition);
        }

        return updateTenantJobsInternal(id, request);
    }

    /**
     * Update a job status.
     */
    public TenantStatusFullResponse updateTenantJobsInternal(String id, @Valid TenantStatusDto request) {

        var tenant = tenantRepository.findById(id);

        var existingStatus = TenantMapper.INSTANCE.mapStatusObject(tenant.getStatus());
        request.jobs = mergeJobs(existingStatus.jobs, request.jobs);

        try {
            var updatedStatusJson = TenantMapper.INSTANCE.mergeJobsIntoStatus(tenant.getStatus(), request);

            updateTenantStatusInDb(tenant, updatedStatusJson);
            var statusDto = TenantMapper.INSTANCE.mapStatusObject(tenant.getStatus());

            var response = new TenantStatusFullResponse();
            response.name = tenant.name;
            response.status = statusDto;

            return response;

            //   return TenantMapper.INSTANCE.mapStatusObject(tenant.getStatus());
        } catch (Exception dbException) {

            throw new RuntimeException("DB update failed: " + dbException.getMessage());
        }
    }

    /**
     * Update an alert status.
     */
    @Transactional
    public TenantStatusDto updateTenantAlerts(String id, @Valid TenantStatusDto request) throws IOException {

        var tenant = tenantRepository.findById(id);
        if (tenant == null) {
            return null;
        }
        var existingStatus = TenantMapper.INSTANCE.mapStatusObject(tenant.getStatus());
        request.jobs = mergeJobs(existingStatus.jobs, request.jobs);

        if (request.jobs != null) {
            request.jobs.forEach(this::applyJobDefinition);
        }

        request.jobs = mergeJobs(existingStatus.jobs, request.jobs);
        try {
            var updatedAlertJson = TenantMapper.INSTANCE.mergeJobsIntoStatus(tenant.getStatus(), request);

            updateTenantStatusInDb(tenant, updatedAlertJson);
            return TenantMapper.INSTANCE.mapStatusObject(tenant.getStatus());
        } catch (Exception dbException) {

            throw new RuntimeException("DB update failed: " + dbException.getMessage());
        }
    }

    //updates the job list existing in the status with the new job value.
    private List<EventStatusDto> mergeJobs(List<EventStatusDto> existingJobs,
                                           List<EventStatusDto> newJobs) {

        if (existingJobs == null && newJobs == null) {
            return Collections.emptyList();
        }
        if (existingJobs == null) {
            return new ArrayList<>(newJobs);
        }
        if (newJobs == null) {
            return new ArrayList<>(existingJobs);
        }
        Map<String, EventStatusDto> map = existingJobs.stream()
                .collect(Collectors.toMap(
                        e -> e.name.toUpperCase(),   // normalize key
                        e -> e
                ));

        // Replace or add
        for (EventStatusDto newJob : newJobs) {
            var oldJob = map.get(newJob.name);
            if (oldJob != null) {

                if (newJob.getStart() == null && oldJob.getStart() != null) {
                    newJob.setStart(oldJob.getStart());
                }
                if (newJob.getEnd() == null && oldJob.getEnd() != null) {
                    newJob.setEnd(oldJob.getEnd());
                }
                if (newJob.properties == null || newJob.properties.isEmpty()) {
                    newJob.properties = oldJob.properties;
                }
            }
            map.put(newJob.name, newJob);


        }
        return new ArrayList<>(map.values());
    }


    public TenantGroupStatus createTenantGroup(String tenantId) {

        var tenant = tenantRepository.findById(tenantId);
        var groupPath = "/" + namespace + "/tenants/" + tenant.name;

        try {
            String groupId = groupManagement.getGroupId(groupPath);

            if (groupId != null) {
                return TenantGroupStatus.EXISTS;
            }

            Map<String, List<String>> attributes = Map.of(
                    "tenantId", List.of(tenant.id),
                    "description", List.of(tenant.description)
            );

            groupManagement.createGroup(
                    "/" + namespace + "/tenants",
                    tenant.name,
                    List.of("admin", "viewer"),
                    attributes
            );

            return TenantGroupStatus.EXISTS;

        } catch (Exception e) {
            throw new ServiceUnavailableException("Client unavailable");
        }
    }


    private TenantGroupStatus getGroupStatus(Tenant tenant) {
        var groupPath = "/" + namespace + "/tenants/" + tenant.name;
        try {
            return groupManagement.getGroupId(groupPath) != null
                    ? TenantGroupStatus.EXISTS
                    : TenantGroupStatus.NOT_FOUND;
        } catch (Exception e) {
            return TenantGroupStatus.UNKNOWN;
        }
    }

    /**
     * Notify ams that tenant is created and should initialize the corresponding event process
     *
     * @param id,    the tenant's id
     * @param alert, the alert to be sent to AMS
     * @return TenantStatusDto
     */
    public TenantStatusDto notifyAms(String id, AlertDefinitionRequest alert) {
        var now = Instant.now();
        var tenant = tenantRepository.findById(id);

        if (alert.properties.containsKey("tenant_name") && !alert.properties.get("tenant_name").equals(tenant.name)) {
            throw new BadRequestException("Value of property 'name' differs from tenant's name: " + tenant.name);
        }

        validateAlertProperties(alert.name, alert.properties);

        alert.getProperties().put("tenant_id", id);
        alert.setCreatedAt(String.valueOf(now));
        send(id, alert,"");


        var statusOpt = tenantRepository.fetchTenantStatus(id);
        if (!statusOpt.isEmpty()) {
            return TenantMapper.INSTANCE.mapStatusObject(statusOpt.get());
        }
        return null;
    }

    /**
     * Notify ams that tenant is created and should initialize the corresponding event process
     *
     * @param id,    the tenant's id
     * @param alert, the alert to be sent to AMS
     * @return TenantStatusDto
     */
    public TenantStatusDto notifyAmsCheckReadiness(String id, AlertDefinitionRequest alert) {
        var now = Instant.now();
        var tenant = tenantRepository.findById(id);

        if (alert.properties.containsKey("tenant_name") && !alert.properties.get("tenant_name").equals(tenant.name)) {
            throw new BadRequestException("Value of property 'name' differs from tenant's name: " + tenant.name);
        }

        validateAlertProperties(alert.name, alert.properties);

        alert.getProperties().put("tenant_id", id);
        alert.setCreatedAt(String.valueOf(now));
        send(id, alert,"A request is sent to the monitoring service to validate that the necessary data and configuration are in place prior to starting the monitoring process");


        var statusOpt = tenantRepository.fetchTenantStatus(id);
        if (!statusOpt.isEmpty()) {
            return TenantMapper.INSTANCE.mapStatusObject(statusOpt.get());
        }
        return null;
    }

    // send notifications to AMS to initialize ams and mongo
    private void sendNotifications(Tenant tenant) {

        String createdAt = String.valueOf(Instant.now());

        send(tenant.id, buildAlert(EventName.INIT_AMS, tenant, createdAt), "");
        send(tenant.id, buildAlert(EventName.INIT_MONGO, tenant, createdAt), "");
        send(tenant.id, buildAlert(EventName.INIT_COMPUTE_ENGINE, tenant, createdAt), "");
    }

    private AlertDefinitionRequest buildAlert(EventName eventName, Tenant tenant, String createdAt) {
        AlertDefinitionRequest alert = new AlertDefinitionRequest();
        alert.name = eventName.name();
        alert.setCreatedAt(createdAt);
        alert.setProperties(Map.of(
                "tenant_id", tenant.id,
                "tenant_name", tenant.name
        ));
        return alert;
    }

    private void send(String id, AlertDefinitionRequest alert, String eventMsg) {

        final boolean hasCustomMsg =
                eventMsg != null && !eventMsg.isEmpty();


        // INITIALISING message
        final String publishingMsg =
                hasCustomMsg
                        ? eventMsg
                        : "Event notification: " + alert.name +
                        " is sent to Messaging Service for publishing";


        // Your special INITIALISED message (only when eventMsg exists)
        final String customInitialisedMsg =
                "A request is initialised to the Messaging Service " +
                        "to validate that the necessary data and configuration " +
                        "are in place prior to starting the monitoring process";


        // FINAL INITIALISED message
        final String initialisedMsg =
                hasCustomMsg
                        ? customInitialisedMsg
                        : "Event notification: " + alert.name +
                        " is initialised to Messaging Service for publishing";

// ✅ CUSTOM FAILED
        final String customFailedMsg =
                "A request to validate that the necessary data and configuration " +
                        "are in place prior to starting the monitoring process, failed to be published the Messaging Service.";


// FAILED
        final String failedMsg =
                hasCustomMsg
                        ? customFailedMsg
                        : "Event notification: " + alert.name +
                        " failed to be initialised to Messaging Service";
        try {
            final Instant now = Instant.now();

            final ObjectMapper objectMapper = new ObjectMapper();
            final String json = objectMapper.writeValueAsString(alert);

            Log.infof(
                    "Sending to Messaging Service | project=%s | topic=%s | tenantId=%s | event=%s",
                    amsService.getProject(),
                    amsService.getTopic(),
                    id,
                    alert.name.toUpperCase()
            );


            final String encodedData =
                    Base64.getEncoder().encodeToString(json.getBytes());

            final PublishRequest.Message message =
                    new PublishRequest.Message();

            message.setData(encodedData);

            final PublishRequest publishData = new PublishRequest();
            publishData.setMessages(List.of(message));


            // 1. INITIALISING
            updateTenantAlerts(
                    id,
                    setAlert(
                            alert.name,
                            EventStatus.INITIALISING,
                            publishingMsg,
                            now,
                            alert.properties
                    )
            );


            // 2. Async publish
            CompletableFuture
                    .runAsync(
                            () -> amsService.publishMessage(publishData),
                            executorService
                    )


                    // 3. INITIALISED
                    .thenRun(() -> {
                        try {
                            updateTenantAlerts(
                                    id,
                                    setAlert(
                                            alert.name,
                                            EventStatus.INITIALISED,
                                            initialisedMsg,
                                            now,
                                            alert.properties
                                    )
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })


                    // 4. FINAL RESULT
                    .whenComplete((ignored, throwable) -> {

                        try {
                            if (throwable == null) {

                                updateTenantAlerts(
                                        id,
                                        setAlert(
                                                alert.name,
                                                EventStatus.INITIALISED,
                                                initialisedMsg,
                                                now,
                                                alert.properties
                                        )
                                );

                                Log.debugf(
                                        "Messaging Service publish succeeded for tenantId=%s, alert=%s",
                                        id,
                                        alert.name
                                );

                            } else {

                                Log.errorf(
                                        throwable,
                                        "Messaging Service publish failed for tenantId=%s, alert=%s",
                                        id,
                                        alert.name
                                );

                                updateTenantAlerts(
                                        id,
                                        setAlert(
                                                alert.name,
                                                EventStatus.FAILED_INITIALISATION,
                                                failedMsg,
                                                now,
                                                alert.properties
                                        )
                                );
                            }

                        } catch (Exception e) {
                            Log.error("Failed to update tenant status", e);
                        }
                    });

        } catch (Exception e) {

            Log.error("Failed to send alert to Messaging Service", e);

            throw new RuntimeException(
                    "Failed to send event notification: " + alert.name,
                    e
            );
        }
    }

    //building an alert with info
    private TenantStatusDto setAlert(String eventName, EventStatus status, String message, Instant start, Map<String, String> properties) {
        var tenantStatus = new TenantStatusDto();
        tenantStatus.jobs = new ArrayList<>();
        var alert = new EventStatusDto();
        alert.start = start;
        alert.setName(eventName);
        alert.setStatus(status.name());
        alert.setMessage(message);

        if (properties != null && !properties.isEmpty()) {
            alert.properties = new HashMap<>(properties); // copy
        }
        tenantStatus.jobs.add(alert);
//        if (status.equals(EventStatus.FAILED_INITIALISATION) || status.equals(EventStatus.INITIALISED)) {
//            alert.end = Instant.now();
//        }
        return tenantStatus;
    }

    //sets the status of jobs and alerts to UNKNOWN
    private TenantStatusDto setDefaultStatus() {
        var dto = new TenantStatusDto();
        dto.jobs = new ArrayList<>();

        for (var def : TenantJobEvent.values()) {
            EventStatusDto job = new EventStatusDto();
            job.setName(def.key());
            job.setMode(def.modeValue());
            job.setStatus(EventStatus.UNKNOWN.name());
            if (def.isManual()) {
                job.setMessage("Waiting for manual administrator action");
            }
            dto.jobs.add(job);
        }

        return dto;
    }

    /**
     * Get a tenant's status.
     */
    public TenantStatusFullResponse getTenantStatus(String id) {
        var resultOpt = tenantRepository.fetchTenantNameAndStatus(id);

        if (resultOpt.isPresent()) {
            Object[] result = resultOpt.get();
            String name = (String) result[0];
            String statusString = (String) result[1];

            TenantStatusDto statusDto = TenantMapper.INSTANCE.mapStatusObject(statusString);

            TenantStatusFullResponse response = new TenantStatusFullResponse();
            response.name = name;
            response.status = statusDto;

            return response;
        }

        return null; // or Optional<TenantStatusFullResponse> if you prefer
    }

    private void applyJobDefinition(EventStatusDto job) {
        if (job == null || job.name == null) return;

        var def = org.grnet.status.enums.TenantJobEvent
                .fromKey(job.name)
                .orElseThrow(() -> new BadRequestException("Unknown job name: " + job.name));

        job.setName(def.key());
        job.setMode(def.modeValue());
    }

    private void validateJobsMode(List<EventStatusDto> jobs, EventMode expectedMode) {

        if (jobs == null || jobs.isEmpty()) {
            return;
        }

        for (EventStatusDto job : jobs) {
            if (job == null || job.getName() == null) {
                continue;
            }

            var def = TenantJobEvent
                    .fromKey(job.getName())
                    .orElseThrow(() -> new BadRequestException("Unknown job name: " + job.getName()));

            if (def.mode() != expectedMode) {
                throw new BadRequestException("Job '" + def.key() + "' is " + def.mode().name().toLowerCase()
                        + " and cannot be updated");
            }
        }
    }

    private void validateAlertProperties(String eventName, Map<String, String> props) {
        if (props == null || props.isEmpty()) return;

        var def = TenantJobEvent.fromKey(eventName)
                .orElseThrow(() -> new jakarta.ws.rs.BadRequestException("Unknown job name: " + eventName));

        for (var k : props.keySet()) {
            var keyEnum = TenantJobProperty.fromKey(k)
                    .orElseThrow(() -> new jakarta.ws.rs.BadRequestException(
                            "Unknown property key '" + k + "' for job '" + def.key() + "'"
                    ));

            if (!def.allowedProperties().contains(keyEnum)) {
                throw new jakarta.ws.rs.BadRequestException(
                        "Property '" + keyEnum.key() + "' is not allowed for job '" + def.key() + "'"
                );
            }
        }
    }

    /**
     * Check the readiness of a tenant by Id.
     */
    @Transactional
    public WebApiTenantReadiness checkReadiness(String id) {
        try {
            var tenant = tenantRepository.findById(id);
            return webApiService.retrieveTenantReadinessWebApi(tenant.id);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

}