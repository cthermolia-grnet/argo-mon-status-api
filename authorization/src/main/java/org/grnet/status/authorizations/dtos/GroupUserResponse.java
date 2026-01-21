package org.grnet.status.authorizations.dtos;

import java.util.List;

public class GroupUserResponse {

    public String id;

    public String username;
    public String firstName;
    public String lastName;

    public String email;

    public List<UserGroupInfoDto> tenants;
}
