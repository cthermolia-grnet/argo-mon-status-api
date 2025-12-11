package org.grnet.status.authorizations.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GroupResponse {

    @JsonProperty("results")
    public List<Group> results;

    @JsonProperty("count")
    public int count;
}
