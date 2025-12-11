package org.grnet.status.authorizations.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GroupMembership {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("active")
    public Boolean active;

    @JsonProperty("requireApproval")
    public Boolean requireApproval;

    @JsonProperty("requireApprovalForExtension")
    public Boolean requireApprovalForExtension;

    @JsonProperty("visibleToNotMembers")
    public Boolean visibleToNotMembers;

    @JsonProperty("multiselectRole")
    public Boolean multiselectRole;

    @JsonProperty("commentsNeeded")
    public Boolean commentsNeeded;

    @JsonProperty("commentsLabel")
    public String commentsLabel;

    @JsonProperty("commentsDescription")
    public String commentsDescription;

    @JsonProperty("groupRoles")
    public List<String> groupRoles;

    public void setGroupRoles(List<String> roles) {
        this.groupRoles = roles;
    }
}
