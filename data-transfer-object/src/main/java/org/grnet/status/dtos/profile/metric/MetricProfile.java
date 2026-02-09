package org.grnet.status.dtos.profile.metric;

import java.util.List;

public class MetricProfile {

    private String id;
    private List<ServiceMetrics> services;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ServiceMetrics> getServices() {
        return services;
    }

    public void setServices(List<ServiceMetrics> services) {
        this.services = services;
    }
}
