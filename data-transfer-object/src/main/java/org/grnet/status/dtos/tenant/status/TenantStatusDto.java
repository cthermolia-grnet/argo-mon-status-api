package org.grnet.status.dtos.tenant.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.tenant.metadata.InstanceDto;

import java.util.List;

@Schema(name = "TenantStatusDto", description = "Represents the configuration of a tenant's status  info.")

public class TenantStatusDto {

        @Schema(
                type = SchemaType.ARRAY,
                implementation = EventStatusDto.class,
                description = "List of the event statuses"
        )
        @JsonProperty("jobs")
        @Valid
        // @NotNull(message = "instance can not be null")
        public List<EventStatusDto> jobs;

}
