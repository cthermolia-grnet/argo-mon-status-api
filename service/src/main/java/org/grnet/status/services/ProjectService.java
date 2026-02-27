package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.project.ProjectUpdateDto;

import org.grnet.status.mappers.ProjectMapper;
import org.grnet.status.repositories.ProjectRepository;
import org.jboss.logging.Logger;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Service responsible for managing project entities.
 */
@ApplicationScoped
public class ProjectService {

    @Inject
    ProjectRepository projectRepository;

    private static final Logger LOG = Logger.getLogger(ReportService.class);

    /**
     * Creates a new project.
     *
     * @param projectRequestDto project creation request
     * @return created project response
     */
    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto projectRequestDto) {

        var entity = ProjectMapper.INSTANCE.dtoToProject(projectRequestDto);

        projectRepository.persist(entity);

        return ProjectMapper.INSTANCE.projectToDto(entity);
    }


    /**
     * Retrieves a project by its identifier.
     *
     * @param id project identifier
     * @return project response
     */
    @Transactional
    public ProjectResponseDto getProjectById(String id) {

        var project = projectRepository.findById(id);

        return ProjectMapper.INSTANCE.projectToDto(project);
    }

    /**
     * Updates an existing project by its identifier.
     *
     * @param id project identifier
     * @param request project update request
     * @return updated project response
     */
    @Transactional
    public ProjectResponseDto updateProjectById(String id, ProjectUpdateDto request) {

        var project = projectRepository.findById(id);

        var updateProject = ProjectMapper.INSTANCE.updateToProject(request, project);
        updateProject.setUpdatedAt(Timestamp.from(Instant.now()));

        return ProjectMapper.INSTANCE.projectToDto(updateProject);
    }

    /**
     * Deletes a project by its identifier.
     *
     * @param id project identifier
     */
    @Transactional
    public void deleteById(String id) {
        var project=projectRepository.findById(id);
        if (!project.getTenantProjects().isEmpty()) {
            throw new IllegalStateException("Deleting Project... Project cannot be deleted because it belongs to one or more tenants.");
        }
        projectRepository.deleteById(id);
    }


    /**
     * Retrieves a paginated list of projects with optional search and sorting.
     *
     * @param page 0-based page index
     * @param size page size
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @param uriInfo request context for pagination links
     * @return paginated list of projects
     */
    @Transactional
    public PageResource<ProjectResponseDto> getAllProjectsByPageAndSize(int page, int size, String search, String sort, String order, UriInfo uriInfo) {

        var projects = projectRepository.fetchProjectByPage(page, size, search, sort, order);

        return new PageResource<>(projects, ProjectMapper.INSTANCE.projectsToDtos(projects.list()), uriInfo);
    }

}
