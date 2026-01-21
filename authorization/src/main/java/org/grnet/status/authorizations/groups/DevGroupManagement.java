package org.grnet.status.authorizations.groups;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.grnet.status.authorizations.dtos.*;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@IfBuildProfile(anyOf = {"dev", "test"})
public class DevGroupManagement implements GroupManagement {

    @Inject
    ObjectMapper objectMapper;

    private static final Logger LOG = Logger.getLogger(DevGroupManagement.class);

    @Override
    public void createGroup(String parentPath, String name, List<String> roles, Map<String, List<String>> attributes) {
        LOG.debugf("DEV: createGroup skipped (%s/%s)", parentPath, name);
    }

    @Override
    public void deleteGroup(String fullGroupPath) {
        LOG.debugf("DEV: deleteGroup skipped (%s)", fullGroupPath);
    }

    @Override
    public List<GroupUser> fetchGroupMembers(String fullPath) {

        LOG.debugf("DEV: fetchGroupMembers returns empty (%s)", fullPath);

        String mockJson = """
                {
                              "results": [
                                  {
                                      "id": "e533bbd9-204b-4642-85e3-b6f9218c51ca",
                                      "group": {
                                          "id": "d1609083-213e-4f39-b9a4-28c1e66af604",
                                          "name": "members",
                                          "path": "/status-pages/members",
                                          "attributes": {
                                              "description": [
                                                  "Members of status-page"
                                              ],
                                              "expiration-notification-period": [
                                                  "21"
                                              ],
                                              "defaultConfiguration": [
                                                  "a0aba987-9efb-4041-b683-312347ecb87b"
                                              ]
                                          }
                                      },
                                      "user": {
                                          "id": "51552389-98b3-4567-b603-5046888ce1b7",
                                          "username": "test@gmail.com",
                                          "emailVerified": true,
                                          "firstName": "Test",
                                          "lastName": "Test",
                                          "email": "test@gmail.com",
                                          "attributes": {
                                              "voPersonID": [
                                                  "test@einfra.grnet.gr"
                                              ],
                                              "localEntitlements": [
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:members:role=member"
                                              ],
                                              "cat_entitlements": [
                                                  "assessment:add78a27-722c-402d-990a-548f1d3994f8",
                                                  "assessment:2e1af1bd-1e92-49b2-a366-1623f4bde72d"
                                              ]
                                          },
                                          "federatedIdentities": [
                                              {
                                                  "identityProvider": "google"
                                              }
                                          ]
                                      },
                                      "status": "ENABLED",
                                      "validFrom": "2026-01-08",
                                      "groupRoles": [
                                          "member"
                                      ],
                                      "direct": true
                                  },
                                  {
                                      "id": "64a75af0-7f54-4101-a9ea-907bee7da98e",
                                      "group": {
                                          "id": "d1609083-213e-4f39-b9a4-28c1e66af604",
                                          "name": "members",
                                          "path": "/status-pages/members",
                                          "attributes": {
                                              "description": [
                                                  "Members of status-page"
                                              ],
                                              "expiration-notification-period": [
                                                  "21"
                                              ],
                                              "defaultConfiguration": [
                                                  "a0aba987-9efb-4041-b683-312347ecb87b"
                                              ]
                                          }
                                      },
                                      "user": {
                                          "id": "6dd6a82f-baa7-4989-bcfa-741ebaa6cf8c",
                                          "username": "test1@grnet-hq.admin.grnet.gr",
                                          "emailVerified": true,
                                          "firstName": "Test1",
                                          "lastName": "Test1",
                                          "email": "test1@admin.grnet.gr",
                                          "attributes": {
                                              "eduPersonAssurance": [
                                                  "https://refeds.org/assurance",
                                                  "https://refeds.org/assurance/IAP/medium",
                                                  "https://refeds.org/assurance/ID/eppn-unique-no-reassign",
                                                  "https://refeds.org/assurance/ID/unique",
                                                  "https://refeds.org/assurance/IAP/low"
                                              ],
                                              "terms_and_conditions": [
                                                  "1744118394"
                                              ],
                                              "voPersonID": [
                                                  "test1@einfra.grnet.gr"
                                              ],
                                              "eduPersonScopedAffiliation": [
                                                  "staff@grnet-hq.admin.grnet.gr"
                                              ],
                                              "localEntitlements": [
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:role=super_admin",
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:members:role=member"
                                              ],
                                              "displayName": [
                                                  "Test1 Test1"
                                              ],
                                              "schacHomeOrganization": [
                                                  "grnet.gr"
                                              ],
                                              "eduPersonPrincipalName": [
                                                  "test1@grnet-hq.admin.grnet.gr"
                                              ],
                                              "cat_entitlements": [
                                                  "assessment:ad5f3c93-bc3b-490b-a922-ae3594df1bd9"
                                              ]
                                          },
                                          "federatedIdentities": [
                                              {
                                                  "identityProvider": "National Infrastructures for Research and Technology - GRNET"
                                              }
                                          ]
                                      },
                                      "status": "ENABLED",
                                      "validFrom": "2026-01-16",
                                      "effectiveMembershipExpiresAt": "2026-11-25",
                                      "effectiveGroupId": "47eb944b-9859-4bdc-97f1-337b95597e61",
                                      "groupRoles": [
                                          "member"
                                      ],
                                      "direct": true
                                  },
                                  {
                                      "id": "68a915d5-57b2-4589-8581-544912fd5a4c",
                                      "group": {
                                          "id": "d1609083-213e-4f39-b9a4-28c1e66af604",
                                          "name": "members",
                                          "path": "/status-pages/members",
                                          "attributes": {
                                              "description": [
                                                  "Members of status-page"
                                              ],
                                              "expiration-notification-period": [
                                                  "21"
                                              ],
                                              "defaultConfiguration": [
                                                  "a0aba987-9efb-4041-b683-312347ecb87b"
                                              ]
                                          }
                                      },
                                      "user": {
                                          "id": "60d78c7f-7985-4553-9e51-6a18020b07db",
                                          "username": "test2@grnet-hq.admin.grnet.gr",
                                          "emailVerified": true,
                                          "firstName": "Test2",
                                          "lastName": "Test2",
                                          "email": "test2@admin.grnet.gr",
                                          "attributes": {
                                              "eduPersonAssurance": [
                                                  "https://refeds.org/assurance",
                                                  "https://refeds.org/assurance/IAP/medium",
                                                  "https://refeds.org/assurance/ID/eppn-unique-no-reassign",
                                                  "https://refeds.org/assurance/ID/unique",
                                                  "https://refeds.org/assurance/IAP/low"
                                              ],
                                              "terms_and_conditions": [
                                                  "1752757234"
                                              ],
                                              "voPersonID": [
                                                  "test2@einfra.grnet.gr"
                                              ],
                                              "eduPersonScopedAffiliation": [
                                                  "staff@grnet-hq.admin.grnet.gr"
                                              ],
                                              "localEntitlements": [
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TESTTENANT:role=admin",
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TESTTENANTINVITE:role=viewer",
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:members:role=member"
                                              ],
                                              "displayName": [
                                                  "Test2 Test2"
                                              ],
                                              "schacHomeOrganization": [
                                                  "grnet.gr"
                                              ],
                                              "eduPersonPrincipalName": [
                                                  "test2@grnet-hq.admin.grnet.gr"
                                              ],
                                              "cat_entitlements": [
                                                  "assessment:590c84ef-54cf-4b82-81c6-df3d4e295ac1",
                                                  "assessment:b4b91b61-56d9-458f-9c5e-9ffdf50796eb",
                                                  "assessment:b9cd2434-ac37-4414-9181-1a6f64bf2e71"
                                              ]
                                          },
                                          "federatedIdentities": [
                                              {
                                                  "identityProvider": "National Infrastructures for Research and Technology - GRNET"
                                              }
                                          ]
                                      },
                                      "status": "ENABLED",
                                      "validFrom": "2025-12-17",
                                      "groupRoles": [
                                          "member"
                                      ],
                                      "direct": true
                                  },
                                  {
                                      "id": "8e7a0537-1db3-4ecb-be9f-1470d4ed840e",
                                      "group": {
                                          "id": "d1609083-213e-4f39-b9a4-28c1e66af604",
                                          "name": "members",
                                          "path": "/status-pages/members",
                                          "attributes": {
                                              "description": [
                                                  "Members of status-page"
                                              ],
                                              "expiration-notification-period": [
                                                  "21"
                                              ],
                                              "defaultConfiguration": [
                                                  "a0aba987-9efb-4041-b683-312347ecb87b"
                                              ]
                                          }
                                      },
                                      "user": {
                                          "id": "beb70def-ff91-49b0-bce7-c23b762981fe",
                                          "username": "test3@gmail.com",
                                          "emailVerified": true,
                                          "firstName": "Test3",
                                          "lastName": "Test3",
                                          "email": "test3@gmail.com",
                                          "attributes": {
                                              "terms_and_conditions": [
                                                  "1750324712"
                                              ],
                                              "voPersonID": [
                                                  "test3@einfra.grnet.gr"
                                              ],
                                              "localEntitlements": [
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:role=super_admin",
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TESTTENANTINVITE:role=viewer",
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:members:role=member",
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANT%20TEST:role=admin"
                                              ],
                                              "cat_entitlements": [
                                                  "assessment:52962555-7721-4d53-b768-9f8fdce2dad1",
                                                  "assessment:a398415f-ff51-4a80-bcf7-eaa69784248b",
                                                  "assessment:bf5dbfff-286d-4cba-83a4-b95e5b2adb8d",
                                                  "assessment:59542713-6d3b-4ae9-918f-1d6f273d5ce4",
                                                  "assessment:82d7d18a-1d15-480a-a9d7-df9e2f028605",
                                                  "assessment:76c6d1fe-cb32-4058-b925-1c55952af4e4",
                                                  "assessment:e88e57ce-5a4c-4ab4-ac73-525c2fe356c6",
                                                  "assessment:a5dbca05-f4cc-4ab3-92d8-9c29b21c23fc",
                                                  "assessment:eef82da0-8c93-489b-b02d-67c0a27e1bbd",
                                                  "assessment:f7b4373e-190e-4f14-8268-faedc06acaff",
                                                  "assessment:c53c34ab-320d-4102-9293-e3bfd8af09d7",
                                                  "assessment:3d5d2bc9-c4e4-44bb-9cbe-fd7a321695c2",
                                                  "assessment:9a8bfb3f-6362-4593-8e6d-c463f50752ac",
                                                  "assessment:0670795b-5222-49a2-9bdf-22716f21f6e7",
                                                  "assessment:603eb743-a904-4a67-9de3-a8207200026c",
                                                  "assessment:1db92932-20b8-461f-bb7a-42f0e60c9059",
                                                  "assessment:1f117eb9-1a9a-411e-a967-8a53d0c3f766",
                                                  "assessment:b08d06ae-0c65-41b4-a51d-849838546a88",
                                                  "assessment:7bf23d31-3d8e-4258-b1ee-46ee5e1fea53",
                                                  "assessment:3d8e60fb-e7d4-4dab-aa99-eb92703a48c5",
                                                  "assessment:fab8e9a4-7f12-4764-b923-d471145629b8",
                                                  "assessment:19410765-c87e-4043-90a3-41607ebb610f",
                                                  "assessment:05d8faf1-7a01-4c1b-9816-60ba0507f72c",
                                                  "assessment:aa7d9dda-1b07-4b89-bc23-92cfd4b2173b",
                                                  "assessment:57d1ae0f-3d25-4e7c-a387-85f049b53dbd",
                                                  "assessment:b6de4536-6e75-41b7-9e35-12005428940c",
                                                  "assessment:e024e1bf-ac2a-411c-8f56-672fdbe08103",
                                                  "assessment:03ff1de9-9639-438f-befa-82dd199a2933",
                                                  "assessment:347ebcd4-c404-4fe8-9564-151523254682",
                                                  "assessment|8c903d27-6fec-4227-bb29-f27b2e0d7910",
                                                  "assessment:61ccb5c6-a619-4bb7-9ac9-53a75f3e74bf",
                                                  "assessment:3118d5b8-2c60-43cb-8e2d-5257531482d8",
                                                  "assessment:70547933-f52c-4d2c-99b5-d98e0d4f2341"
                                              ]
                                          },
                                          "federatedIdentities": [
                                              {
                                                  "identityProvider": "google"
                                              }
                                          ]
                                      },
                                      "status": "ENABLED",
                                      "validFrom": "2025-12-21",
                                      "effectiveMembershipExpiresAt": "2026-11-11",
                                      "effectiveGroupId": "47eb944b-9859-4bdc-97f1-337b95597e61",
                                      "groupRoles": [
                                          "member"
                                      ],
                                      "direct": true
                                  },
                                  {
                                      "id": "e951c2c6-eb55-40cc-a8af-bc036eced292",
                                      "group": {
                                          "id": "d1609083-213e-4f39-b9a4-28c1e66af604",
                                          "name": "members",
                                          "path": "/status-pages/members",
                                          "attributes": {
                                              "description": [
                                                  "Members of status-page"
                                              ],
                                              "expiration-notification-period": [
                                                  "21"
                                              ],
                                              "defaultConfiguration": [
                                                  "a0aba987-9efb-4041-b683-312347ecb87b"
                                              ]
                                          }
                                      },
                                      "user": {
                                          "id": "1a29496e-5c35-43b2-a9d8-61b0283adce2",
                                          "username": "test4@grnet-hq.admin.grnet.gr",
                                          "emailVerified": true,
                                          "firstName": "Test4",
                                          "lastName": "Test4",
                                          "email": "test4@admin.grnet.gr",
                                          "attributes": {
                                              "eduPersonAssurance": [
                                                  "https://refeds.org/assurance",
                                                  "https://refeds.org/assurance/IAP/medium",
                                                  "https://refeds.org/assurance/ID/eppn-unique-no-reassign",
                                                  "https://refeds.org/assurance/ID/unique",
                                                  "https://refeds.org/assurance/IAP/low"
                                              ],
                                              "terms_and_conditions": [
                                                  "1737555136"
                                              ],
                                              "voPersonID": [
                                                  "test4@einfra.grnet.gr"
                                              ],
                                              "eduPersonScopedAffiliation": [
                                                  "staff@grnet-hq.admin.grnet.gr"
                                              ],
                                              "localEntitlements": [
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:role=super_admin",
                                                  "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:members:role=member"
                                              ],
                                              "displayName": [
                                                  "Test4 Test4"
                                              ],
                                              "schacHomeOrganization": [
                                                  "grnet.gr"
                                              ],
                                              "eduPersonPrincipalName": [
                                                  "test4@grnet-hq.admin.grnet.gr"
                                              ],
                                              "cat_entitlements": [
                                                  "assessment:ffa726ca-36ad-4db1-8327-15c140a80ad9",
                                                  "assessment:51e28c1a-e413-46f1-a1cd-37528df10533",
                                                  "assessment:818e8ae7-744f-41cd-8214-6cb08def674c",
                                                  "assessment:d19fe826-fda6-4201-858f-987f3fa78c5d",
                                                  "assessment:334f5d5d-e423-4eaa-afd3-cc6f48acb646",
                                                  "assessment:6e46951b-95fa-4f86-a79a-5708706e743b",
                                                  "assessment:0ffa26d3-2871-44c1-812b-a7fd4648dcc2",
                                                  "assessment:bf5dbfff-286d-4cba-83a4-b95e5b2adb8d",
                                                  "assessment:c53c34ab-320d-4102-9293-e3bfd8af09d7",
                                                  "assessment:f7b4373e-190e-4f14-8268-faedc06acaff",
                                                  "assessment:eef82da0-8c93-489b-b02d-67c0a27e1bbd",
                                                  "assessment:a5dbca05-f4cc-4ab3-92d8-9c29b21c23fc",
                                                  "assessment:e88e57ce-5a4c-4ab4-ac73-525c2fe356c6",
                                                  "assessment:971ed599-2b93-4de5-855d-d70cbbf87ffb",
                                                  "assessment:c1805bdd-b475-499d-88de-1104012f9d47",
                                                  "assessment:51c55819-e6b4-47b8-9435-335e1b4c06cc",
                                                  "assessment:ea687a5b-6183-4e85-a1a6-23de737880ed",
                                                  "assessment:f632fcc7-1d41-46b5-888f-5c49007285c1",
                                                  "assessment:a398415f-ff51-4a80-bcf7-eaa69784248b",
                                                  "assessment:a9590f60-1954-4eb6-9c4c-217e3ff3c07f",
                                                  "assessment:b20fe5b8-b710-4428-9aad-b6b66a81e07b",
                                                  "assessment:a37328c6-c225-4479-bf4b-6fbe1b3c72b0",
                                                  "assessment:89b4a011-fb11-44c0-a8a2-9bbd72fce4f3",
                                                  "assessment:e93f8f0d-0687-418d-a002-dcc877034cf4",
                                                  "assessment:87af56c0-137b-485e-9fb7-3aca2870fdbb",
                                                  "assessment:ac3be8c1-cb2d-45c5-95be-4247d76d45ff",
                                                  "assessment:63f3e7e5-6c2d-4dba-9b5a-2d1f3c8d1eb9",
                                                  "assessment:cfed9cf7-cf8b-4846-9467-824a62e21dee",
                                                  "assessment:8b413c91-9f11-43bb-8b90-05f54dd57776",
                                                  "assessment:b01caa0c-c851-4f87-a02f-f182209ea06f",
                                                  "assessment:3eed7631-5661-4de8-9538-23fba99683c8",
                                                  "assessment:309b64f0-eb67-4165-8e48-6c6325c3316f",
                                                  "assessment:922420d0-2252-4e57-a66f-301090cd832d",
                                                  "assessment:faa01508-725c-4a0f-952a-379eebaa6cb6",
                                                  "assessment:0b75bed4-341c-4dbb-887f-31a580ac3f65",
                                                  "assessment:50d0c7a5-8198-4119-9291-79faa3777e62",
                                                  "assessment:c22ab839-b343-4508-bb7b-db8c496733f0",
                                                  "assessment:78c05747-9eeb-4665-b28f-8909658148cc"
                                              ]
                                          },
                                          "federatedIdentities": [
                                              {
                                                  "identityProvider": "National Infrastructures for Research and Technology - GRNET"
                                              }
                                          ]
                                      },
                                      "status": "ENABLED",
                                      "validFrom": "2026-01-20",
                                      "effectiveMembershipExpiresAt": "2026-11-25",
                                      "effectiveGroupId": "47eb944b-9859-4bdc-97f1-337b95597e61",
                                      "groupRoles": [
                                          "member"
                                      ],
                                      "direct": true
                                  }
                              ],
                              "count": 5
                          }
        """;

        try {
            var mocks =  objectMapper.readValue(mockJson, GroupMembersResponse.class);
            return mocks.results.stream()
                    .map(entry -> entry.user)
                    .toList();

        } catch (Exception e) {
           return  List.of();
        }
    }

    @Override
    public void addRole(String groupId, String role) {
        LOG.debugf("DEV: addRole skipped (%s role=%s)", groupId, role);
    }

    @Override
    public String getGroupId(String fullPath) {
        LOG.debugf("DEV: getGroupId skipped (%s)", fullPath);
        return null;
    }

    @Override
    public void updateConfiguration(String groupId, List<String> groupRoles) {
        LOG.debugf("DEV: updateConfiguration skipped (%s)", groupId);
    }

    @Override
    public List<GroupUser> fetchGroupMembersByRole(String fullPath, String role) {
        LOG.debugf("DEV: fetchGroupMembers returns empty (%s)", fullPath, role);
        return List.of();
    }

    @Override
    public void addGroupMember(String fullPath, String username, String role) {
        LOG.debugf("DEV: addGroupMemberIdempotent skipped (user=%s, group=%s)", username, fullPath);
    }
}