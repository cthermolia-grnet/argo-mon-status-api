package org.grnet.status.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.general.ExistResponseDto;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.status.StatusGroupRequestDto;
import org.grnet.status.dtos.statuspage.*;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.enums.ArgoItemStatusEnum;
import org.grnet.status.mappers.GeneralMapper;
import org.grnet.status.mappers.StatusPageMapper;
import org.grnet.status.repositories.StatusPageRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.utils.EncryptUtil;
import org.grnet.status.services.utils.ImageUploadUtil;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class StatusPageService {

    @Inject
    StatusPageRepository statusPageRepository;

    @Inject
    TenantRepository tenantRepository;

    @Inject
    StatusService statusService;

    @Inject
    ImageUploadUtil imageUploadUtil;

    @Inject
    ReportService reportService;

    @Inject
    UserService userService;


    @Inject
    ObjectMapper objectMapper;


    @ConfigProperty(name = "api.server.url")
    String apiServerUrl;

    @ConfigProperty(name = "base.upload.logo.dir")
    String baseUploadLogoDir;

    /**
     * Create a new status statuspage.
     */
    @Transactional
    public StatusPageResponseDto createStatusPage(String tenantId, StatusPageRequestDto request, String userId) {

        validateGroupsExist(tenantId, request.reportId, request.config.groups);
        validateTheming(request.config);
        checkIfExistSlug(request.slug, null);

        var report = reportService.fetchReportById(tenantId, request.reportId);

        var entity = StatusPageMapper.INSTANCE.dtoToEntity(request);
        entity.setTenant(tenantRepository.findById(tenantId));
        entity.setUserId(userId);
        entity.setReport(report.info.name);
        statusPageRepository.persist(entity);

        apiServerUrl = apiServerUrl.replaceAll("/+$", "");

        // --- handle logo
        var logo = request.config.theming.logo;
        if (logo != null && logo.startsWith("data:image/")) {
            imageUploadUtil.validateBase64Image(logo);
            var savedPath = imageUploadUtil.saveBase64Image(baseUploadLogoDir, logo, entity.getId(), "/logos/");
            var fullUrl = apiServerUrl + savedPath;
            entity.setConfig(updateLogo(entity.getConfig(), fullUrl));
        }

        return StatusPageMapper.INSTANCE.entityToDto(entity);
    }


    /**
     * Update an existing status statuspage.
     */
    @Transactional
    public StatusPageResponseDto updateStatusPage(String tenantId, String statusPageId, StatusPageUpdateRequestDto request) {

        var entity = statusPageRepository.searchByIdOptional(statusPageId)
                .orElseThrow(() -> new IllegalArgumentException("StatusPage not found with id " + statusPageId));

        checkIfExistSlug(request.slug, statusPageId);
        validateGroupsExist(tenantId, request.reportId, request.config.groups);
        validateTheming(request.config);

        StatusPageMapper.INSTANCE.updateToEntity(request, entity);

        var report = reportService.fetchReportById(tenantId, request.reportId);
        entity.setReport(report.info.name);

        apiServerUrl = apiServerUrl.replaceAll("/+$", "");


        // --- Handle logo *after* mapper to avoid overwrite ---
        var logo = request.config.theming.logo;

        if (logo != null && logo.startsWith("data:image/")) {
            imageUploadUtil.validateBase64Image(logo);
            imageUploadUtil.deleteImageIfExists(baseUploadLogoDir, statusPageId);
            var savedPath = imageUploadUtil.saveBase64Image(baseUploadLogoDir, logo, entity.getId(), "/logos/");
            var fullUrl = apiServerUrl + savedPath;
            entity.setConfig(updateLogo(entity.getConfig(), fullUrl));

        } else if (logo == null || logo.isBlank()) {
            imageUploadUtil.deleteImageIfExists(baseUploadLogoDir, entity.getId());
            entity.setConfig(removeLogo(entity.getConfig()));

        } else {
            entity.setConfig(updateLogo(entity.getConfig(), logo));
        }

        return StatusPageMapper.INSTANCE.entityToDto(entity);
    }


    /**
     * Get a statuspage by ID.
     */
    public StatusPageResponseDto getStatusPageById(String id) {

        var statusPage = statusPageRepository.findById(id);

        return StatusPageMapper.INSTANCE.entityToDto(statusPage);
    }


    /**
     * Retrieves a page of Subjects submitted by the specified user.
     *
     * @param page    The index of the page to retrieve (starting from 0).
     * @param size    The maximum number of Subjects to include in a page.
     * @param uriInfo The Uri Info.
     * @param tenantId  The ID of the user.
     * @return A list of SubjectResponse objects representing the submitted Subjects in the requested page.
     */
    public PageResource<StatusPageResponseDto> getStatusPageByUserAndPage(int page, int size, UriInfo uriInfo, String tenantId, String userId) {

        var tenant = tenantRepository.findById(tenantId);
        var isViewer = isViewerForTenantFromProfile(tenant.name, userId);

        var statusPages = isViewer
                ? statusPageRepository.fetchStatusPageByTenantAndAndUserAndPage(page, size, tenantId, userId)
                : statusPageRepository.fetchStatusPagesByTenant(page, size, tenantId);

        return new PageResource<>(statusPages, StatusPageMapper.INSTANCE.entitiesToDtos(statusPages.list()), uriInfo);
    }


    public PageResource<StatusPageResponseDto> getStatusPageByPage(int page, int size, UriInfo uriInfo) {

        var statusPages = statusPageRepository.fetchStatusPageByPage(page, size);

        return new PageResource<>(statusPages, StatusPageMapper.INSTANCE.entitiesToDtos(statusPages.list()), uriInfo);
    }


    /**
     * List all pages.
     */
    public List<StatusPageResponseDto> listAll() {
        return StatusPageMapper.INSTANCE.entitiesToDtos(statusPageRepository.listAll());
    }

    /**
     * Check if a slug is already used.
     */
    public ExistResponseDto slugExists(String slug) {

        var exist = statusPageRepository.find("slug", slug).firstResultOptional().isPresent();

        return GeneralMapper.INSTANCE.toExistResponse(slug, exist);
    }

    /**
     * Delete a statuspage by ID.
     */
    @Transactional
    public void deleteStatusPage(String id) {

        var entity = statusPageRepository.findById(id);
        if (entity == null) {
            throw new IllegalArgumentException("StatusPage not found with id " + id);
        }
        imageUploadUtil.deleteImageIfExists(baseUploadLogoDir, id);

        statusPageRepository.delete(entity);
    }


    //----------------------------------------------------------------------------------------------------
    //  HELPER METHODS
    //----------------------------------------------------------------------------------------------------
    public void checkIfExistSlug(String slug, String currentId) {
        var existing = statusPageRepository.find("slug", slug)
                .firstResultOptional();

        if (existing.isPresent()) {
            // CREATE case
            if (currentId == null) {
                throw new BadRequestException("A page with slug '" + slug + "' already exists.");
            }

            // UPDATE case
            if (!existing.get().getId().equals(currentId)) {
                throw new BadRequestException("A page with slug '" + slug + "' already exists.");
            }
        }
    }

    public void validateGroupsExist(String tenantId, String reportId, List<StatusPageGroupDto> groups) {

        var argoGroups = statusService.getStatusGroups(tenantId, reportId);

        if (argoGroups == null || argoGroups.isEmpty()) {
            throw new IllegalArgumentException("Unable to retrieve groups from ARGO for this report.");
        }

        // Extract all valid endpoint names from ARGO
        var validNames = argoGroups.stream()
                .map(g -> g.name)
                .collect(java.util.stream.Collectors.toSet());

        // Validate each item inside each group
        for (var group : groups) {
            for (var item : group.list) {
                if (!validNames.contains(item.name)) {
                    throw new IllegalArgumentException(
                            "Service '" + item.name + "' is not a valid ARGO item for this report."
                    );
                }

                if (!ArgoItemStatusEnum.isValid(item.status)) {
                    throw new IllegalArgumentException(
                            "Invalid ARGO status '" + item.status + "' for '" + item.name + "'."
                    );
                }
            }
        }
    }

    public void validateTheming(StatusPageConfigDto config) {

        var theming = config.theming;

        // Validate logo (new Base64 upload or existing HTTPS URL) ---
        if (theming.logo != null && !theming.logo.isBlank()) {
            String logo = theming.logo.trim();

            if (logo.startsWith("data:image/")) {
                if (!logo.contains("base64,")) {
                    throw new IllegalArgumentException("Invalid Base64 image format for logo");
                }
            } else if (!logo.matches("^(https?://).+")) {
                throw new IllegalArgumentException("Invalid logo format. Expected a Base64 data URI or HTTPS URL.");
            }
        }

        // validate color
        if (theming.color != null && !theming.color.matches("^#([A-Fa-f0-9]{6})$")) {
            throw new IllegalArgumentException("Invalid color format, expected #RRGGBB");
        }

        // validate icon type
        var validIcons = Set.of("led", "icon", "none");
        if (!validIcons.contains(theming.status.icon)) {
            throw new IllegalArgumentException("Invalid icon type: " + theming.status.icon);
        }

        // validate status text mode
        var validTextModes = Set.of("text", "badge", "none");
        if (!validTextModes.contains(theming.status.text)) {
            throw new IllegalArgumentException("Invalid status text mode: " + theming.status.text);
        }

        // validate columns
        var validColumns = Set.of("one", "two");
        if (!validColumns.contains(theming.columns)) {
            throw new IllegalArgumentException("Invalid column layout: " + theming.columns);
        }
    }

    public String updateLogo(String configJson, String newLogoPath) {
        try {
            var root = configJson == null || configJson.isBlank()
                    ? objectMapper.createObjectNode()
                    : (ObjectNode) objectMapper.readTree(configJson);

            var theming = root.with("theming");
            theming.put("logo", newLogoPath);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update logo in config JSON", e);
        }
    }

    public String removeLogo(String configJson) {
        try {
            if (configJson == null || configJson.isBlank()) return configJson;
            var root = (ObjectNode) objectMapper.readTree(configJson);
            var theming = (ObjectNode) root.with("theming");
            theming.putNull("logo");
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove logo from config JSON", e);
        }
    }

    public String extractLogo(String configJson) {
        if (configJson == null || configJson.isBlank()) return null;
        try {
            var root = (ObjectNode) objectMapper.readTree(configJson);
            return root.path("theming").path("logo").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isViewerForTenantFromProfile(String tenantName, String userId) {

        var user = userService.getUserProfile(userId);

        var role = user.groups.stream()
                .filter(g -> g != null && g.name != null)
                .filter(g -> g.name.equalsIgnoreCase(tenantName))
                .map(g -> g.role == null ? null : g.role.trim())
                .findFirst()
                .orElse(null);

        if (role == null || role.isBlank()) {
            return true;
        }

        return "viewer".equalsIgnoreCase(role);
    }
}
