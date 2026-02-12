package org.grnet.status.services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.authorizations.dtos.GroupUserResponse;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenantproject.TenantProjectDeleteDto;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.dtos.tenantproject.TenantProjectDto;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQueryImpl;
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
import java.util.stream.Collectors;

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

    @Inject
    GroupManagementService groupManagementService;

    // --------------------------------------------------------------------
    // SUPER_ADMIN
    // --------------------------------------------------------------------
    @Transactional
    public InformativeResponse assign(TenantProjectRequestDto request) {

        var response = new InformativeResponse();

        for (var projectId : request.projectIds) {
            if (projectRepository.findByIdOptional(projectId).isEmpty()) {
                throw new WebApplicationException("Project not found: " + projectId, 404);
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

    public PageResource<TenantProjectDto> getTenantsProjects(int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        var dto = tenantProjectJunctionRepository.fetchTenantsProjectsByPageAndSize(page, size, search, sort, order);

        return new PageResource<>(dto, TenantProjectMapper.INSTANCE.listToDtos(dto.list()), uriInfo);
    }


    public PageResource<TenantResponseDto> getTenantsByProject(String projectId, int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        var dto =  tenantRepository.findByProjectId(projectId, page, size, search, sort, order);

        return new PageResource<>(dto, TenantMapper.INSTANCE.tenantsToDtos(dto.list()), uriInfo);


    }

    public PageResource<ProjectResponseDto> getProjectsByTenant(String tenantId, int page, int size, UriInfo uriInfo, String search, String sort, String order) {

        var dto =  projectRepository.findByTenantId(tenantId, page, size, search, sort, order);

        return new PageResource<>(dto, ProjectMapper.INSTANCE.projectsToDtos(dto.list()), uriInfo);
    }

    public PageResource<GroupUserResponse> getMembersByTenant(String tenantId, int page, int size, UriInfo uriInfo) {

        var tenant = tenantRepository.findById(tenantId);

        var response = groupManagementService.getMembers("tenants/"+tenant.name, page * size, size, "");

        var members = response
                .results
                .stream()
                .map(g->g.user)
                .map(gu -> {
                    var user = new GroupUserResponse();
                    user.id = gu.id;
                    user.email = gu.email;
                    user.username = gu.username;
                    user.firstName = gu.firstName;
                    user.lastName = gu.lastName;
                    user.tenants = gu.getTenants();
                    return user;
                })
                .collect(Collectors.toList());

        var pageable = new PageQueryImpl<GroupUserResponse>();

        pageable.list = members;
        pageable.index = page;
        pageable.size = size;
        pageable.count = response.count;
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }


    @Transactional
    public void deleteAssignment(TenantProjectDeleteDto request) {

        tenantProjectJunctionRepository.deleteByTenantAndProject(request.tenantId, request.projectId);
    }
}
