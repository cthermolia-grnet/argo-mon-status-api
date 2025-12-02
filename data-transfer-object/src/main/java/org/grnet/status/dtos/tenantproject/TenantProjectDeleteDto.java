package org.grnet.status.dtos.tenantproject;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.repositories.ProjectRepository;
import org.grnet.status.repositories.TenantRepository;

import java.util.List;

public class TenantProjectDeleteDto {

    @Schema(
            type = SchemaType.ARRAY,
            description = "The project identifier",
            example = "proj-32262f66f6e1"
    )
    @JsonProperty("project_id")
    @NotNull(message = "Must provide project_id")
    @NotFoundEntity(repository = ProjectRepository.class, message = "There is no Project with id: ")
    public String projectId;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Tenant identifier",
            example = "5205e18d-2e12-4b04-b72b-f9b2b7b973a3"
    )
    @JsonProperty("tenant_id")
    @NotNull(message = "Must provide tenant_id")
    @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with id: ")
    public String tenantId;
}
