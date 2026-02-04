package org.grnet.status.dtos.report;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
@Schema(description = "WebApiReportResponse returns the argo-web-api response of a report")

public class WebApiReportResponse {

    @Schema(description = "Status of the API response")
    public Status status;

    @Schema(description = "List of reports")
    public List<FullReportResponseDto> data;

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
