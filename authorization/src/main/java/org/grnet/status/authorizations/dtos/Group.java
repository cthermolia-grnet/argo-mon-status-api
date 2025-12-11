package org.grnet.status.authorizations.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class Group {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("path")
    public String path;

    @JsonProperty("attributes")
    public Map<String, List<String>> attributes;

    @JsonProperty("extraSubGroups")
    public List<Group> extraSubGroups;
}
