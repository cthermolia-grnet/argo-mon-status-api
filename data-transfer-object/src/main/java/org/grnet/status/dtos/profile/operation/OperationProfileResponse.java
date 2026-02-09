package org.grnet.status.dtos.profile.operation;

import java.util.List;

public class OperationProfileResponse {

    private Status status;

    private List<OperationProfile> data;

    // getters & setters
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<OperationProfile> getData() {
        return data;
    }

    public void setData(List<OperationProfile> data) {
        this.data = data;
    }
}
