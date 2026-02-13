package org.grnet.status.dtos.tenant.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "TenantStatusDto", description = "Represents the configuration of a tenant's status  info.")

public class TenantStatusDto {

        @Schema(
                type = SchemaType.ARRAY,
                implementation = EventStatusDto.class,
                description = "List of the event statuses",
                example=" [\n" +
                        "    {\n" +
                        "      \"name\": \"init_ams\",\n" +
                        "      \"status\": \"in_progress\",\n" +
                        "      \"start\": \"2025-10-22T12:44:48Z\",\n" +
                        "      \"end\": \"2025-10-22T12:44:48Z\",\n" +
                        "      \"message\": \"Created topic in ams\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"name\": \"init_mongo\",\n" +
                        "      \"status\": \"in_progress\",\n" +
                        "      \"start\": \"2025-10-22T12:44:48Z\",\n" +
                        "      \"end\": \"2025-10-22T12:44:48Z\",\n" +
                        "      \"message\": \"Creating indexes in mongo\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"name\": \"create_domain_names\",\n" +
                        "      \"status\": \"completed\",\n" +
                        "      \"start\": \"2025-10-22T12:44:48Z\",\n" +
                        "      \"end\": \"2025-10-22T12:44:48Z\",\n" +
                        "      \"message\": \"Creating domain names\"\n" +
                        "    }\n" +
                        "  ]\n"
        )
        @JsonProperty("jobs")
        @Valid
        // @NotNull(message = "instance can not be null")
        public List<@Valid EventStatusDto> jobs;

        @JsonProperty("jobs")
        public void setJobs(List<EventStatusDto> jobs) {
                if (jobs == null) {
                        this.jobs = null;
                        return;
                }

                // Filter out CHECK_READINESS
                this.jobs = jobs.stream()
                        .filter(job -> !"CHECK_READINESS".equalsIgnoreCase(job.getName()))
                        .toList();
        }
}
