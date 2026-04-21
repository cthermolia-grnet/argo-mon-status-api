package org.grnet.status.authorizations.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PartialGroup {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("path")
    private String path;

    @JsonProperty("group_roles")
    private Map<String, String> groupRoles;

    public PartialGroup(String id, String name, String path, Map<String, String> groupRoles) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.groupRoles = groupRoles;
    }

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(Map<String, String> groupRoles) {
        this.groupRoles = groupRoles;
    }
}
