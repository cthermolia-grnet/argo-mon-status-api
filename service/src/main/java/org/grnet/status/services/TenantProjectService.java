package org.grnet.status.services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenantproject.TenantProjectDeleteDto;
import org.grnet.status.dtos.tenantproject.TenantProjectDto;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.entities.Project;
import org.grnet.status.entities.Tenant;
import org.grnet.status.entities.TenantProjectJunction;
import org.grnet.status.mappers.ProjectMapper;
import org.grnet.status.mappers.TenantMapper;
import org.grnet.status.mappers.TenantProjectMapper;
import org.grnet.status.repositories.ProjectRepository;
import org.grnet.status.repositories.TenantProjectJunctionRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.util.Utility;

import java.util.UUID;

/**
 * Service responsible for managing tenant to project assignments.
 */
@ApplicationScoped
public class TenantProjectService {

    @Inject
    TenantRepository tenantRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    TenantProjectJunctionRepository tenantProjectJunctionRepository;

    @Inject
    Utility utility;

    // --------------------------------------------------------------------
    // SUPER_ADMIN
    // --------------------------------------------------------------------
    /**
     * Assigns projects to a tenant by adding missing links and removing obsolete links.
     *
     * @param request tenant project assignment request
     * @return informative response
     */
    @Transactional
    public InformativeResponse assign(TenantProjectRequestDto request) {

        var response = new InformativeResponse();

        for (var projectId : request.projectIds) {
            if (projectRepository.findByIdOptional(projectId).isEmpty()) {
                throw new WebApplicationException("Assigning project to tenant... Project not found: " + projectId, 404);
            }
        }

        var existing = tenantProjectJunctionRepository.findByTenantId(request.tenantId);

        // empty list -> delete everything
        if (request.projectIds.isEmpty()) {
            for (var junction : existing) {
                tenantProjectJunctionRepository.delete(junction);
            }
            response.code = 200;
            response.message = "All projects removed from Tenant";

            return response;
        }

        var existingProjectIds = existing.stream()
                .map(junction -> junction.project.getId())
                .toList();

        var toAdd = request.projectIds.stream()
                .filter(id -> !existingProjectIds.contains(id))
                .toList();

        var toRemove = existing.stream()
                .filter(j -> !request.projectIds.contains(j.project.getId()))
                .toList();

        for (var junction : toRemove) {
            tenantProjectJunctionRepository.delete(junction);
        }

        for (var projectId : toAdd) {
            var junction = new TenantProjectJunction();
            junction.id = UUID.randomUUID().toString();
            junction.tenant = Panache.getEntityManager().getReference(Tenant.class, request.tenantId);
            junction.project = Panache.getEntityManager().getReference(Project.class, projectId);
            junction.createdBy = utility.getUserUniqueIdentifier();
            tenantProjectJunctionRepository.persist(junction);
        }

        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            response.code = 200;
            response.message = "No changes applied. Assignments already up to date.";
        } else {
            response.code = 200;
            response.message = String.format(
                    "Tenant project assignments updated. Added: %d, Removed: %d",
                    toAdd.size(), toRemove.size()
            );
        }

        return response;
    }

    /**
     * Retrieves a paginated list of tenant project assignments with optional search and sorting.
     *
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @return paginated list of tenant project assignments
     */
    public PageResource<TenantProjectDto> getTenantsProjects(int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        var dto = tenantProjectJunctionRepository.fetchTenantsProjectsByPageAndSize(page, size, search, sort, order);

        return new PageResource<>(dto, TenantProjectMapper.INSTANCE.listToDtos(dto.list()), uriInfo);
    }


    /**
     * Retrieves a paginated list of tenants linked to the specified project.
     *
     * @param projectId project identifier
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @return paginated list of tenants
     */
    public PageResource<TenantResponseDto> getTenantsByProject(String projectId, int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        var dto =  tenantRepository.findByProjectId(projectId, page, size, search, sort, order);

        return new PageResource<>(dto, TenantMapper.INSTANCE.tenantsToDtos(dto.list()), uriInfo);


    }

    /**
     * Retrieves a paginated list of projects linked to the specified tenant.
     *
     * @param tenantId tenant identifier
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @return paginated list of projects
     */
    public PageResource<ProjectResponseDto> getProjectsByTenant(String tenantId, int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        var dto =  projectRepository.findByTenantId(tenantId, page, size, search, sort, order);

        return new PageResource<>(dto, ProjectMapper.INSTANCE.projectsToDtos(dto.list()), uriInfo);
    }


    /**
     * Deletes a tenant to project assignment.
     *
     * @param request tenant project delete request
     */
    @Transactional
    public void deleteTenantProjectAssignment(TenantProjectDeleteDto request) {

        tenantProjectJunctionRepository.deleteByTenantAndProject(request.tenantId, request.projectId);
    }
}
