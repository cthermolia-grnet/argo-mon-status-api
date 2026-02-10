package org.grnet.status.dtos.readiness;

import org.grnet.status.dtos.Status;

import java.util.List;

public class WebApiTenantReadiness {
    private Status status;
    private TenantReadiness data;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TenantReadiness getData() {
        return data;
    }

    public void setData(TenantReadiness data) {
        this.data = data;
    }
}

