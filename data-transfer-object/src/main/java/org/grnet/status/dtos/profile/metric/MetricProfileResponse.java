package org.grnet.status.dtos.profile.metric;

import org.grnet.status.dtos.Status;

import java.util.List;

public class MetricProfileResponse {

    private Status status;
    private List<MetricProfile> data;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<MetricProfile> getData() {
        return data;
    }

    public void setData(List<MetricProfile> data) {
        this.data = data;
    }
}
