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
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.tenant.ContactDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.entities.Contact;
import org.grnet.status.entities.Tenant;
import org.grnet.status.enums.ContactType;
import org.grnet.status.exceptions.CustomRuntimeException;
import org.grnet.status.mappers.TenantMapper;
import org.grnet.status.repositories.ContactRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.clients.ArgoWebApiClientFactory;
import org.grnet.status.services.utils.EncryptUtil;
import org.grnet.status.services.utils.ImageUploadUtil;

import java.io.IOException;
import java.util.*;

@ApplicationScoped

public class TenantService {

    @Inject
    TenantRepository tenantRepository;
    @Inject
    ContactRepository contactRepository;
//
//    @Inject ArgoWebApiClient client;
//    @Inject String decryptedSecret;

    @ConfigProperty(name = "base.upload.logo.dir")
    String baseUploadTenantsImagesDir;

    @Inject
    ImageUploadUtil imageUploadUtil;
    @ConfigProperty(name = "api.server.url")
    String apiServerUrl;
    @Inject
    EncryptUtil encryptUtil;

    @Inject
    ArgoWebApiClientFactory argoWebApiClientFactory;
    @Inject
    AuthGroupAsyncService authGroupAsyncService;

    @ConfigProperty(name = "admin.web.api.encrypted.secret")
    String encryptedSecret;
    @ConfigProperty(name = "web.api.url")
    String webapi;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String namespace;


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
        remoteTenantId = createTenantInWebApi(request);
        tenantCreatedRemotely = true;
        try {
            TenantMapper.INSTANCE.mapMetadata(request,tenant);
            writeInDB(request, tenant, remoteTenantId, userId);
            return TenantMapper.INSTANCE.tenantToDto(tenant);
        } catch (Exception e) {
            // If tenant was created remotely, but something failed locally, rollback remote creation
            if (tenantCreatedRemotely && remoteTenantId != null) {
                try {
                    var client = produceClient();
                    var decryptedSecret = produceDecryptedKey();
                    client.deleteTenant(remoteTenantId, decryptedSecret); // Make sure you have this method in your client
                } catch (Exception rollbackEx) {
                    // Log rollback failure, but do not mask original exception
                    System.err.println("Rollback failed for tenant id " + remoteTenantId + ": " + rollbackEx.getMessage());
                }
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
            webtenant = retrieveTenantWebApi(id);
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

            var client = produceClient();
            var decryptedSecret = produceDecryptedKey();
            client.deleteTenant(id, decryptedSecret);

            // 4. Delete orphan contacts
            deleteOrphanContacts(oldContacts);

        } catch (RuntimeException e) {
            int status = 500;

            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }

            throw new WebApplicationException(e.getMessage(), status);
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
                // 1. Delete from DB first (inside transaction)
                Set<Contact> oldContacts = new HashSet<>(t.getContacts());

                tenantRepository.delete(t);
                oldContacts.stream().forEach(c -> {
                    contactRepository.delete(c);
                });

                imageUploadUtil.deleteImageIfExists(baseUploadTenantsImagesDir, t.name);


                var client = produceClient();
                var decryptedSecret = produceDecryptedKey();
                // 2. Only after DB delete succeeds, call external API
                client.deleteTenant(t.id, decryptedSecret);

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
        var client = produceClient();
        var decryptedSecret = produceDecryptedKey();
        TenantRequestDto previousRemoteState = retrievePreviousTenantWebApi(client, decryptedSecret, id);
        // ------------------------------
        // 2. Update remote API first
        // ------------------------------

        updateTenantWebApi(client, decryptedSecret, request, id);
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
                var webApiRequestPreviousState=TenantMapper.INSTANCE.toWebApiRequest(previousRemoteState);
                client.updateTenant(id, decryptedSecret,webApiRequestPreviousState);
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
                webtenant = retrieveTenantWebApi(t.id);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            webtenant.contacts = TenantMapper.INSTANCE.contactsToDtos(t.getContacts());
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

    private TenantResponseDto retrieveTenantWebApi(String id) throws JsonProcessingException {
        var tenant = tenantRepository.findById(id);
        TenantWebApiGetResponse webApiResponse = null;
        try {

            var client = produceClient();
            var decryptedSecret = produceDecryptedKey();
            webApiResponse = client.getTenant(decryptedSecret, id);

        } catch (RuntimeException e) {
            int status = 500; // default fallback
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            var message = e.getMessage();
            throw new WebApplicationException("tenant with id " + id + "failed in api " + message, status);
        }
        return TenantMapper.INSTANCE.webApiTenantToDto(tenant, webApiResponse);
    }

    private String createTenantInWebApi(TenantRequestDto request) {
        try {

            var client = produceClient();
            var decryptedSecret = produceDecryptedKey();
            var webApiRequest=TenantMapper.INSTANCE.toWebApiRequest(request);
            var apiResponse = client.createTenant(decryptedSecret, webApiRequest);
            return apiResponse.getData().getId();
        } catch (WebApplicationException e) {

            WebApplicationException wae = (WebApplicationException) e;
            int status = wae.getResponse().getStatus();
            var message = wae.getMessage();
            if (status == 409) {
                var optTenant = tenantRepository.fetchTenantByName(request.info.name);
                if (optTenant.isPresent()) {
                    message = message + ". Existing tenant in Argo Mon Status API has id: " + optTenant.get().id;
                } else {
                    message = message + ". Tenant exists in Argo Web Api but not in Argo Mon Status API";
                }
            }
            throw new WebApplicationException(message, status);
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

    private TenantRequestDto retrievePreviousTenantWebApi(ArgoWebApiClient client, String decryptedSecret, String id) {
        try {
            var remoteExisting = client.getTenant(decryptedSecret, id);
            return TenantMapper.INSTANCE.webApiTenantToTenantRequestDto(
                    remoteExisting.getData().get(0).getInfo()
            );
        } catch (Exception e) {
            throw new WebApplicationException("Cannot fetch remote state for rollback", 500);
        }

    }

    private void updateTenantWebApi(ArgoWebApiClient client, String decryptedSecret, TenantRequestDto request, String id) {
        try {
            var webApiRequest=TenantMapper.INSTANCE.toWebApiRequest(request);
            client.updateTenant(id, decryptedSecret, webApiRequest);
        } catch (Exception e) {
            throw new WebApplicationException("Remote API update failed: " + e.getMessage(), 502);
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

        TenantMapper.INSTANCE.mapMetadata(request,tenant);
        tenantRepository.persist(tenant);
        tenantRepository.flush(); // force errors
    }

    private ArgoWebApiClient produceClient() {
        // var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
        return argoWebApiClientFactory.buildClient(webapi);

    }

    public String produceDecryptedKey() {
        return encryptUtil.decrypt(encryptedSecret);
    }
}