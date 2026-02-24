package org.grnet.status.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.dtos.general.ExistResponseDto;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.report.FullReportResponseDto;
import org.grnet.status.dtos.statuspage.*;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQueryImpl;
import org.grnet.status.entities.StatusPage;
import org.grnet.status.enums.ArgoItemStatusEnum;
import org.grnet.status.mappers.GeneralMapper;
import org.grnet.status.mappers.StatusPageMapper;
import org.grnet.status.repositories.StatusPageRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.utils.ImageUploadUtil;
import org.apache.commons.lang3.StringUtils;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.netty.util.AsciiString.contains;

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
        FullReportResponseDto report = null;
        try {
            report = reportService.fetchReportById(tenantId, request.reportId);
        } catch (WebApplicationException e) {
            throw new WebApplicationException("Creating Status Page... " + " Failed to create status page for report with id: " + request.reportId + " Received message:\n" + e.getMessage());

        }
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
                .orElseThrow(() -> new IllegalArgumentException("Updating StatusPage... StatusPage not found with id " + statusPageId));

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
     * @param page     The index of the page to retrieve (starting from 0).
     * @param size     The maximum number of Subjects to include in a page.
     * @param uriInfo  The Uri Info.
     * @param tenantId The ID of the user.
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
            throw new IllegalArgumentException("Deleting StatusPage... StatusPage not found with id " + id);
        }
        imageUploadUtil.deleteImageIfExists(baseUploadLogoDir, id);

        statusPageRepository.delete(entity);
    }

    public PageResource<StatusPageResponseDto> getAccessibleStatusPages(String userId, String search, int page, int size, UriInfo uriInfo) {

        Log.info("Fetching accessible status pages...");

        var user = userService.getUserProfile(userId);

        if (isSuperAdmin(user)) {

            var statusPages = statusPageRepository.fetchStatusPageByPage(page, size);
            var dtos = StatusPageMapper.INSTANCE.entitiesToDtos(statusPages.list());

            if (StringUtils.isNotBlank(search)) {
                var lower = search.trim().toLowerCase();
                dtos = dtos.stream()
                        .filter(sp ->
                                contains(sp.id, lower) ||
                                        contains(sp.slug, lower) ||
                                        contains(sp.name, lower) ||
                                        contains(sp.tenantName, lower)
                        )
                        .toList();
            }

            var pageable = new PageQueryImpl<StatusPageResponseDto>();
            pageable.list = dtos;
            pageable.index = page;
            pageable.size = size;
            pageable.count = statusPages.count();
            pageable.page = Page.of(page, size);

            return new PageResource<>(pageable, uriInfo);
        }

        if (user == null || user.groups == null || user.groups.isEmpty()) {
            var pageable = new PageQueryImpl<StatusPageResponseDto>();
            pageable.list = List.of();
            pageable.index = page;
            pageable.size = size;
            pageable.count = 0;
            pageable.page = Page.of(page, size);
            return new PageResource<>(pageable, uriInfo);
        }

        var all = new ArrayList<StatusPage>();

        for (var g : user.groups) {

            if (g == null || g.name == null || g.name.isBlank()) {
                continue;
            }

            var tenantName = g.name.trim();
            var role = g.role == null ? "" : g.role.trim();

            var tenant = tenantRepository.findTenantByNameOptional(tenantName).orElse(null);
            if (tenant == null) {
                continue;
            }

            var isViewer = role.isBlank() || "viewer".equalsIgnoreCase(role);

            var pages = isViewer
                    ? statusPageRepository.listByTenantAndUser(tenant.getId(), userId)
                    : statusPageRepository.listByTenant(tenant.getId());

            if (pages != null && !pages.isEmpty()) {
                all.addAll(pages);
            }
        }

        // de-dupe
        var unique = all.stream()
                .filter(sp -> sp != null && sp.getId() != null)
                .collect(Collectors.toMap(
                        StatusPage::getId,
                        sp -> sp,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        var merged = new ArrayList<>(unique.values());

        // sort desc
        merged.sort((a, b) -> {
            var at = a.getCreatedAt();
            var bt = b.getCreatedAt();
            if (at == null && bt == null) return 0;
            if (at == null) return 1;
            if (bt == null) return -1;
            return bt.compareTo(at);
        });

        // search
        if (StringUtils.isNotBlank(search)) {
            var lower = search.trim().toLowerCase();

            merged = (ArrayList<StatusPage>) merged.stream()
                    .filter(sp ->
                            contains(sp.getId(), lower) ||
                                    contains(sp.getSlug(), lower) ||
                                    contains(sp.getName(), lower) ||
                                    contains(sp.getTenant() != null ? sp.getTenant().getName() : null, lower)
                    )
                    .collect(Collectors.toList());
        }

        long total = merged.size();
        int from = Math.max(0, page * size);
        int to = Math.min(merged.size(), from + size);
        var slice = (from >= to) ? List.<StatusPage>of() : merged.subList(from, to);

        var dtos = StatusPageMapper.INSTANCE.entitiesToDtos(slice);

        var pageable = new PageQueryImpl<StatusPageResponseDto>();
        pageable.list = dtos;
        pageable.index = page;
        pageable.size = size;
        pageable.count = total;
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }

    @Transactional
    public void deleteAll() {
        statusPageRepository.deleteAll();
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
                throw new BadRequestException("Checking slug.... A page with slug '" + slug + "' already exists.");
            }

            // UPDATE case
            if (!existing.get().getId().equals(currentId)) {
                throw new BadRequestException("Checking slug... A page with slug '" + slug + "' already exists.");
            }
        }
    }

    public void validateGroupsExist(String tenantId, String reportId, List<StatusPageGroupDto> groups) {

        var argoGroups = statusService.getStatusGroups(tenantId, reportId);

        if (argoGroups == null || argoGroups.isEmpty()) {
            throw new IllegalArgumentException("Validating Groups... No groups exist or failed to be retrieved for report with id: " + reportId);
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
                            "Validating groups... " + "Service '" + item.name + "' is not a valid item for this report."
                    );
                }

                if (!ArgoItemStatusEnum.isValid(item.status)) {
                    throw new IllegalArgumentException(
                            "Validating groups... " + "Invalid status '" + item.status + "' for '" + item.name + "'."
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
                    throw new IllegalArgumentException("Validating Theme... Invalid Base64 image format for logo");
                }
            } else if (!logo.matches("^(https?://).+")) {
                throw new IllegalArgumentException("Validating Theme... Invalid logo format. Expected a Base64 data URI or HTTPS URL.");
            }
        }

        // validate color
        if (theming.color != null && !theming.color.matches("^#([A-Fa-f0-9]{6})$")) {
            throw new IllegalArgumentException("Validating Theme... Invalid color format, expected #RRGGBB");
        }

        // validate icon type
        var validIcons = Set.of("led", "icon", "none");
        if (!validIcons.contains(theming.status.icon)) {
            throw new IllegalArgumentException("Validating Theme... Invalid icon type: " + theming.status.icon);
        }

        // validate status text mode
        var validTextModes = Set.of("text", "badge", "none");
        if (!validTextModes.contains(theming.status.text)) {
            throw new IllegalArgumentException("Validating Theme... Invalid status text mode: " + theming.status.text);
        }

        // validate columns
        var validColumns = Set.of("one", "two");
        if (!validColumns.contains(theming.columns)) {
            throw new IllegalArgumentException("Validating Theme... Invalid column layout: " + theming.columns);
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
            throw new RuntimeException("Updating logo... Failed to update logo in config JSON", e);
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
            throw new RuntimeException("Removing logo... Failed to remove logo from config JSON", e);
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

        var isSuperAdmin = user.groups.stream().anyMatch(g ->
                g != null
                        && g.name != null
                        && "status-pages".equalsIgnoreCase(g.name.trim())
                        && g.role != null
                        && "super_admin".equalsIgnoreCase(g.role.trim())
        );

        if (isSuperAdmin) {
            return false;
        }

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

    private boolean isSuperAdmin(UserProfileDto user) {
        return user != null
                && user.groups != null
                && user.groups.stream().anyMatch(g ->
                g != null
                        && g.name != null
                        && "status-pages".equalsIgnoreCase(g.name.trim())
                        && g.role != null
                        && "super_admin".equalsIgnoreCase(g.role.trim())
        );
    }
}
