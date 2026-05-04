package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.util.Utility;

/**
 * Service responsible for retrieving authenticated user profile information.
 */
@ApplicationScoped
public class UserService {

    @Inject
    Utility utility;
//
    @Inject
    UserEntitlementsService userEntitlementsService;


    /**
     * Builds the user profile using information from the authenticated token.
     *
     * @param id user identifier
     * @return user profile response
     */
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
