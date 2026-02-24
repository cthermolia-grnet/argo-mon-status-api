package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.grnet.status.authorizations.service.UserEntitlementsService;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.util.Utility;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    Utility utility;

    @Inject
    UserEntitlementsService userEntitlementsService;


    @Transactional
    public UserProfileDto getUserProfile(String id) {

        var userProfile = new UserProfileDto();

        userProfile.id = id;
        userProfile.username = utility.getUsername();
        userProfile.email = utility.getUserEmail();
        userProfile.name = utility.getUserName();
        userProfile.surname = utility.getUserSurname();
        userProfile.groups = userEntitlementsService.getUserEntitlements();
        userProfile.uid = utility.getUid();

        return userProfile;
    }
}
