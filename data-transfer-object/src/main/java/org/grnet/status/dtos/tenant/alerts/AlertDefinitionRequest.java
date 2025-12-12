package org.grnet.status.dtos.tenant.alerts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.constraints.ValidEventName;

import java.util.Map;


@Schema(name = "EventDefinitionDto", description = "Represents the configuration of a event's  info to initialize automation process.")
@Getter
@Setter
public class AlertDefinitionRequest {
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "event name",
            example = "init_ams"
    )
    @JsonProperty("name")
    @ValidEventName
    public String name;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = Map.class,
            description = "properties list",
            example = "{\n" +
                    "    \"tenant_name\": \"DEMO TENANT\"\n" +
                    "  }"
    )
    @JsonProperty("properties")
    @NotNull(message = "Properties can not be null")
    public Map<String, String> properties;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of creating event",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty(value = "created_at", access = JsonProperty.Access.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String createdAt;


}
