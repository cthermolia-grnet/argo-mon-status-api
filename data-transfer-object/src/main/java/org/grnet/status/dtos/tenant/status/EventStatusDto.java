package org.grnet.status.dtos.tenant.status;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.constraints.ValidEventMode;
import org.grnet.status.constraints.ValidEventName;
import org.grnet.status.constraints.ValidEventStatus;

import java.time.Instant;
import java.util.Map;

@Schema(name = "EventStatusDto", description = "Represents the configuration of a tenant's status event info.")
@Getter
@Setter
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
            description = "event status",
            example = "in_progress",
            enumeration = {
                    "unknown",
                    "initializing",
                    "initialized",
                    "failed_initialization",
                    "in_progress",
                    "completed",
                    "failed"
            }
    )
    @JsonProperty("status")
    @ValidEventStatus
    public String status;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of start event",
            example = "2025-10-22T12:44:48Z"
    )

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    @JsonProperty("start")
    public Instant start;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of end event",
            example = "2025-10-22T12:44:48Z"
    )

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
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

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Indicates whether the job is manual or automated.",
            example = "auto",
            enumeration = {"manual", "auto"}
    )
    @ValidEventMode
    @JsonProperty("mode")
    public String mode;

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name == null ? null : name.toUpperCase();
    }
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status == null ? null : status.toUpperCase();
    }
    @JsonProperty("mode")
    public void setMode(String mode) {
        this.mode = mode == null ? null : mode.toUpperCase();
    }
}
