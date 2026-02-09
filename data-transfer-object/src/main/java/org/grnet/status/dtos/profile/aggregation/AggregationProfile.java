package org.grnet.status.dtos.profile.aggregation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AggregationProfile {

    private String id;
    private String name;
    private String namespace;

    @JsonProperty("endpoint_group")
    private String endpointGroup;

    @JsonProperty("metric_operation")
    private String metricOperation;

    @JsonProperty("profile_operation")
    private String profileOperation;

    @JsonProperty("metric_profile")
    private MetricProfileRef metricProfile;

    private List<Group> groups;

    // getters & setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEndpointGroup() {
        return endpointGroup;
    }

    public void setEndpointGroup(String endpointGroup) {
        this.endpointGroup = endpointGroup;
    }

    public String getMetricOperation() {
        return metricOperation;
    }

    public void setMetricOperation(String metricOperation) {
        this.metricOperation = metricOperation;
    }

    public String getProfileOperation() {
        return profileOperation;
    }

    public void setProfileOperation(String profileOperation) {
        this.profileOperation = profileOperation;
    }

    public MetricProfileRef getMetricProfile() {
        return metricProfile;
    }

    public void setMetricProfile(MetricProfileRef metricProfile) {
        this.metricProfile = metricProfile;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}

