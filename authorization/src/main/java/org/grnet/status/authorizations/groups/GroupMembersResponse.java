package org.grnet.status.authorizations.groups;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.grnet.status.authorizations.dtos.GroupMemberEntry;

import java.util.ArrayList;
import java.util.List;

public class GroupMembersResponse {

    public List<GroupMemberEntry> results = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long count = 0L;
}
