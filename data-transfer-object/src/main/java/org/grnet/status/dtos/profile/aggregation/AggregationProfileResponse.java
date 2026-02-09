package org.grnet.status.dtos.profile.aggregation;

import org.grnet.status.dtos.profile.operation.Status;

import java.util.List;

public class AggregationProfileResponse {

    private Status status;
    private List<AggregationProfile> data;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<AggregationProfile> getData() {
        return data;
    }

    public void setData(List<AggregationProfile> data) {
        this.data = data;
    }
}

