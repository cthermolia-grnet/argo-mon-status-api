package org.grnet.status.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.authorizations.groups.AuthGroupAsyncService;
import org.grnet.status.authorizations.entitlements.AccessControlService;
import org.grnet.status.authorizations.resolvers.GroupIdResolver;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.tenant.ContactDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.status.EventStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.entities.Contact;
import org.grnet.status.entities.Tenant;
import org.grnet.status.enums.ContactType;
import org.grnet.status.exceptions.CustomRuntimeException;
import org.grnet.status.mappers.TenantMapper;
import org.grnet.status.repositories.ContactRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.clients.ArgoWebApiClientFactory;
import org.grnet.status.services.util.WebApiService;
import org.grnet.status.services.utils.EncryptUtil;
import org.grnet.status.services.utils.ImageUploadUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped

public class TenantService {

    @Inject
    TenantRepository tenantRepository;
    @Inject
    ContactRepository contactRepository;
    @Inject
    AuthGroupAsyncService authGroupAsyncService;

    @Inject
    AccessControlService accessControlService;

    @Inject
    WebApiService webApiService;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String namespace;

    @Inject
    ImageUploadUtil imageUploadUtil;

    @ConfigProperty(name = "base.upload.logo.dir")
    String baseUploadTenantsImagesDir;

    @ConfigProperty(name = "api.server.url")
    String apiServerUrl;

    public TenantResponseDto create(TenantRequestDto request, String userId) throws IOException {

        var response = createTenant(request, userId);

        try {
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("tenantId", List.of(response.id));
            attributes.put("description", List.of(request.info.description));

            var parentPath = "/" + namespace + "/tenants";

            authGroupAsyncService.createGroup(parentPath, response.info.name, List.of("admin", "viewer"), attributes);

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
            var message = "Tenant with id: " + existTenantOpt.get().id + " already exists in ARGO Mon Status API";
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
        try {
            TenantMapper.INSTANCE.mapMetadata(request, tenant);
            writeInDB(request, tenant, remoteTenantId, userId);
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

            authGroupAsyncService.deleteGroup(groupPath);

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

//                var client = produceClient();
//                var decryptedSecret = produceDecryptedKey();
//                // 2. Only after DB delete succeeds, call external API
//                client.deleteTenant(t.id, decryptedSecret);

            } catch (RuntimeException e) {

                // If DB delete fails -> API delete is NOT executed, as desired

                int status = 500;
                if (e instanceof WebApplicationException) {
                    status = ((WebApplicationException) e).getResponse().getStatus();
                }

                var message = e.getMessage();
                Log.error("ERROR deleting tenant " + t.id + " -> " + status + ": " + message);
                // Optional: if you want to stop the operation:
                // throw new WebApplicationException(message, status);

                // Or continue with the next tenant
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
        var previousRemoteState = TenantMapper.INSTANCE.webApiTenantToTenantRequestDto(
                previousWebApiTenant.getData().get(0).getInfo()
        );
        // ------------------------------
        // 2. Update remote API first
        // ------------------------------

        var webApiRequest = TenantMapper.INSTANCE.toWebApiRequest(request);

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
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            tenantList.add(webtenant);
        });
        return new PageResource<>(tenants, tenantList, uriInfo);
    }


    public PageResource<TenantResponseDto> listAuthorizedTenants(GroupIdResolver tenantNameResolver, int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        var allowedTenantIds = accessControlService.resolveAccessibleGroups(tenantNameResolver);


        if (allowedTenantIds == null) {
            return getTenantsByPageAndSize(page, size, uriInfo, search, sort, order);
        }

        var tenants = tenantRepository.fetchTenantsByIdsAndPageAndSize(allowedTenantIds, page, size, search, sort, order);

        var tenantList = new ArrayList<TenantResponseDto>();

        tenants.list().forEach(t -> {
            TenantResponseDto webtenant = null;
            try {
                var webTenantGetResponse = webApiService.retrieveTenantWebApi(t.id);
                webtenant = TenantMapper.INSTANCE.webApiTenantToDto(t, webTenantGetResponse);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            tenantList.add(webtenant);
        });
        return new PageResource<>(tenants, tenantList, uriInfo);
    }

    private void handleImage(TenantRequestDto request) throws IOException {

        var image = request.info.image;
        if (image != null && image.startsWith("data:image/")) {
            imageUploadUtil.validateBase64Image(image);
            var savedPath = imageUploadUtil.saveBase64Image(baseUploadTenantsImagesDir, image, request.info.name, "/logos/");
            request.info.image = apiServerUrl + savedPath;


        }
        // If not Base64, leave image as-is (null or external URL)

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

    private Tenant writeInDB(TenantRequestDto request, Tenant tenant, String remoteTenantId, String userId) {

        tenant.id = remoteTenantId;
        tenant.updatedBy = userId;
        // var contacts = TenantMapper.INSTANCE.dtosToContacts(request.contacts);
        Set<Contact> contacts = resolveAndMergeContacts(request);

        tenant.setContacts(new HashSet(contacts));
        try {
            tenantRepository.persist(tenant);
            System.out.println("Tenant persisted successfully");
            return tenant;
        } catch (Exception e) {
            System.err.println("Persist failed: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow to keep transactional behavior
        }

    }

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

    private void updateTenantStatusInDb(TenantStatusDto request, Tenant tenant) {
        // Update simple fields:
        var json = TenantMapper.INSTANCE.mapStatusToString(request);
        tenant.setStatus(json);
        tenantRepository.persist(tenant);
        tenantRepository.flush(); // force errors
    }

    /**
     * Update an existing tenant.
     */
    @Transactional
    public TenantStatusDto updateTenantStatus(String id, @Valid TenantStatusDto request) throws IOException {

        var tenant = tenantRepository.findById(id);

        var existingStatus = TenantMapper.INSTANCE.mapStatusObject(tenant.getStatus());
        var existingJobs = existingStatus.jobs;
        request.jobs=mergeJobs(existingJobs,request.jobs);

        try {
            updateTenantStatusInDb(request, tenant);
            return TenantMapper.INSTANCE.mapStatusObject(tenant.getStatus());
        } catch (Exception dbException) {

            throw new RuntimeException("DB update failed: " + dbException.getMessage());
        }
    }
    public List<EventStatusDto> mergeJobs(List<EventStatusDto> existingJobs,
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
                        e -> e.name.toLowerCase(),   // normalize key
                        e -> e
                ));

        // Replace or add
        for (EventStatusDto newJob : newJobs) {
            map.put(newJob.name, newJob);
        }
        return new ArrayList<>(map.values());
    }
}