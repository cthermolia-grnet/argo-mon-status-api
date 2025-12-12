package org.grnet.status.dtos.tenant.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.constraints.ValidEventName;
import org.grnet.status.constraints.ValidEventStatus;
import org.grnet.status.constraints.ValidTopologyType;

import java.time.Instant;

@Schema(name = "EventStatusDto", description = "Represents the configuration of a tenant's status event info.")

public class EventStatusDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "event name",
            example="init_ams"
    )
    @JsonProperty("name")
    @ValidEventName
    public String name;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "event status",
            example="in_progress"
    )
    @JsonProperty("status")
    @ValidEventStatus
    public String status;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of start event",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("start")
    public Instant start;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of end event",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("end")
    public Instant end;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Message of event",
            example = "Creating indexes in mongo"
    )
    @JsonProperty("message")
    public String message;

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name == null ? null : name.toLowerCase();
    }
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status == null ? null : status.toLowerCase();
    }
}
