package org.grnet.status.authorizations.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class GroupMembersResponse {

    public List<GroupMemberEntry> results;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long count;
}
