package org.grnet.status.dtos.tenant.webapi;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "TenantWebApiUpdateResponse", description = "Represents the response of a tenant.")
@Getter @Setter
public class TenantWebApiResponse {

    private int code;
    private String message;
}


