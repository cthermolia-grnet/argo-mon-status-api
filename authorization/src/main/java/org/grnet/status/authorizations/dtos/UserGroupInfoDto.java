package org.grnet.status.authorizations.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserGroupInfoDto {

    @JsonProperty(value = "name")
    public String name;

    @JsonProperty("role")
    public String role;
}
