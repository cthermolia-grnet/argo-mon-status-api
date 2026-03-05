package org.grnet.status.dtos.topology;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class WebApiServiceTypeResponse {

    @Schema(description = "Status of the API response")
    public org.grnet.status.dtos.report.WebApiReportResponse.Status status;

    @Schema(description = "List of service types")
    public List<ServiceTypeDto> data;

    public static class Status {
        @Schema(type = SchemaType.STRING,
                description = "Status message",
                example = "Success")
        public String message;

        @Schema(type = SchemaType.STRING,
                description = "Status code",
                example = "200")
        public String code;
    }
}

