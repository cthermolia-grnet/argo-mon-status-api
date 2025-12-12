package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.project.ProjectUpdateDto;

import org.grnet.status.exceptions.ConflictException;
import org.grnet.status.mappers.ProjectMapper;
import org.grnet.status.repositories.ProjectRepository;
import org.jboss.logging.Logger;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

@ApplicationScoped
public class ProjectService {

    @Inject
    ProjectRepository projectRepository;

    private static final Logger LOG = Logger.getLogger(ReportService.class);

    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto projectRequestDto) {

        var entity = ProjectMapper.INSTANCE.dtoToProject(projectRequestDto);

        projectRepository.persist(entity);

        return ProjectMapper.INSTANCE.projectToDto(entity);
    }


    @Transactional
    public ProjectResponseDto getProjectById(String id) {

        var project = projectRepository.findById(id);

        return ProjectMapper.INSTANCE.projectToDto(project);
    }

    @Transactional
    public ProjectResponseDto updateProjectById(String id, ProjectUpdateDto request) {

        var project = projectRepository.findById(id);

        if (!Objects.equals(project.getName(), request.name)) {
            if (projectRepository.existsByNameForOtherId(request.name, id)) {
                throw new ConflictException("Project name already exists: " + request.name);
            }
        }

        var updateProject = ProjectMapper.INSTANCE.updateToProject(request, project);
        updateProject.setUpdatedAt(Timestamp.from(Instant.now()));

        var dto = ProjectMapper.INSTANCE.projectToDto(updateProject);

        return dto;
    }

    @Transactional
    public void deleteById(String id) {
        var project=projectRepository.findById(id);
        if (!project.getTenantProjects().isEmpty()) {
            throw new IllegalStateException("Project cannot be deleted because it belongs to one or more tenants.");
        }
        projectRepository.deleteById(id);
    }


    @Transactional
    public PageResource<ProjectResponseDto> getAllProjectsByPageAndSize(int page, int size, String search, String sort, String order, UriInfo uriInfo) {

        var projects = projectRepository.fetchProjectByPage(page, size, search, sort, order);

        return new PageResource<>(projects, ProjectMapper.INSTANCE.projectsToDtos(projects.list()), uriInfo);
    }

}
