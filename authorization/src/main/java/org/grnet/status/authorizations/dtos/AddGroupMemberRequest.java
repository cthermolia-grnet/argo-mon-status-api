package org.grnet.status.authorizations.dtos;

import java.util.List;

public class AddGroupMemberRequest {

    public UserRef user;
    public List<String> groupRoles;

    public AddGroupMemberRequest(String username, List<String> groupRoles) {
        this.user = new UserRef(username);
        this.groupRoles = groupRoles;
    }

    public AddGroupMemberRequest() {}

    public static class UserRef {
        public String username;

        public UserRef() {}

        public UserRef(String username) {
            this.username = username;
        }
    }

}
