package org.grnet.status.dtos.tenant.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.constraints.ValidContactType;
import org.grnet.status.enums.ContactType;

@Schema(name = "InternalListDto", description = "Represents the configuration of a tenant's metadata internal list info.")

public class InternalListDto {
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "email",
            example="email@example.com"
    )
    @JsonProperty("email")
    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String email;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "type",
            examples = "ADMIN"
    )
    @JsonProperty("type")
    @ValidContactType
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String  type;
}
