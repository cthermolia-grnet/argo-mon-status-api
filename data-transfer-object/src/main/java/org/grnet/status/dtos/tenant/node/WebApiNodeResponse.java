package org.grnet.status.dtos.tenant.node;

import org.grnet.status.dtos.Status;

public class WebApiNodeResponse {

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
