package org.grnet.status.services;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.grnet.endpoint.scanner.runtime.endpoints.RoleResponse;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpointRepository;
;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;
import org.grnet.status.dtos.role.RoleEndpointAssignmentRequest;
import org.grnet.status.dtos.role.RoleEndpointAssignmentResponse;
import org.grnet.status.dtos.role.SecuredEndpointPerRoleRequest;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleEndpointService {

    @Inject
    RoleEndpointRepository roleEndpointRepository;
    @Inject
    ResourceAuthorizationService resourceAuthorizationService;

    public RoleEndpointAssignmentResponse getAssignedEndpoints() {

        List<RoleEndpoint> roleEndpoints = roleEndpointRepository.findAll();

        Map<String, List<RoleEndpoint>> grouped =
                roleEndpoints.stream()
                        .collect(Collectors.groupingBy(RoleEndpoint::getRoleId));

        List<RoleEndpointAssignmentResponse.RoleAssignment> assignments =
                grouped.entrySet().stream()
                        .map(entry -> {
                            List<RoleEndpoint> group = entry.getValue();
                            RoleEndpoint first = group.get(0);

                            RoleEndpointAssignmentResponse.RoleAssignment ra =
                                    new RoleEndpointAssignmentResponse.RoleAssignment();

                            ra.setRoleId(entry.getKey()); // ✅ safer than first.getRoleId()
                            ra.setRoleName(first.getRoleName());

                            ra.setSecuredEndpointIds(
                                    group.stream()
                                            .map(RoleEndpoint::getSecuredEndpointId)
                                            .distinct()
                                            .collect(Collectors.toList())
                            );

                            return ra;
                        })
                        .collect(Collectors.toList());

        RoleEndpointAssignmentResponse response = new RoleEndpointAssignmentResponse();
        response.setAssignments(assignments);

        return response;
    }

    public RoleEndpointAssignmentResponse getAssignedEndpointsByRoleId(String roleId) {
//        var roleResponse=resourceAuthorizationService.getAllRoles();
//        RoleResponse role = roleResponse.stream()
//                .filter(r -> r.id.equals(roleId))
//                .findFirst()
//                .orElse(null);
//        if (role == null) {
//            throw new NotFoundException(
//                    String.format("Role with id %s not found", roleId)
//            );
//        }

        List<RoleEndpoint> roleEndpoints = roleEndpointRepository.findAll()
                .stream()
                .filter(re -> re.getRoleId().equals(roleId))
                .toList();

        RoleEndpointAssignmentResponse response = new RoleEndpointAssignmentResponse();

        if (roleEndpoints.isEmpty()) {
            response.setAssignments(Collections.emptyList());
            return response;
        }

        RoleEndpoint first = roleEndpoints.get(0);

        RoleEndpointAssignmentResponse.RoleAssignment assignment =
                new RoleEndpointAssignmentResponse.RoleAssignment();

        assignment.setRoleId(roleId);
        assignment.setRoleName(first.getRoleName());

        assignment.setSecuredEndpointIds(
                roleEndpoints.stream()
                        .map(RoleEndpoint::getSecuredEndpointId)
                        .distinct()
                        .collect(Collectors.toList())
        );

        response.setAssignments(List.of(assignment));

        return response;
    }

    @Transactional
    public void assignRolesToEndpoints(RoleEndpointAssignmentRequest request) {

        List<RoleEndpoint> existing = roleEndpointRepository.findAll();

        Set<String> requestedRoleIds = request.getAssignments()
                .stream()
                .map(RoleEndpointAssignmentRequest.RoleAssignment::getRoleId)
                .collect(Collectors.toSet());

        Set<String> dbRoleIds = existing.stream()
                .map(RoleEndpoint::getRoleId)
                .collect(Collectors.toSet());

        // DELETE roles not in request
        Set<String> rolesToDelete = new HashSet<>(dbRoleIds);
        rolesToDelete.removeAll(requestedRoleIds);

        for (String roleId : rolesToDelete) {
            roleEndpointRepository.deleteByRoleId(roleId);
        }

        //  SYNC remaining roles
        for (var assignment : request.getAssignments()) {

            String roleId = assignment.getRoleId();
            String roleName = assignment.getRoleName();

            Set<String> incoming = new HashSet<>(assignment.getSecuredEndpointIds());

            List<RoleEndpoint> roleEntries =
                    roleEndpointRepository.list("role_id", roleId);

            Set<String> existingEndpoints = roleEntries.stream()
                    .map(RoleEndpoint::getSecuredEndpointId)
                    .collect(Collectors.toSet());

            // delete removed endpoints
            Set<String> toDelete = new HashSet<>(existingEndpoints);
            toDelete.removeAll(incoming);

            if (!toDelete.isEmpty()) {
                roleEndpointRepository.deleteByRoleIdAndEndpointIds(roleId, new ArrayList<>(toDelete));
            }

            // insert new endpoints
            Set<String> toInsert = new HashSet<>(incoming);
            toInsert.removeAll(existingEndpoints);

            for (String endpointId : toInsert) {
                RoleEndpoint re = new RoleEndpoint();
                re.setRoleId(roleId);
                re.setRoleName(roleName);
                re.setSecuredEndpointId(endpointId);
                roleEndpointRepository.create(re);
            }
        }
    }


    @Transactional
    public void assignRolesToEndpointsPerRole(String roleId, SecuredEndpointPerRoleRequest request) {

        var roleResponse=resourceAuthorizationService.getAllRoles();
        RoleResponse role = roleResponse.stream()
                .filter(r -> r.id.equals(roleId))
                .findFirst()
                .orElse(null);

        List<RoleEndpoint> roleEntries = roleEndpointRepository.list("role_id",roleId);
        Set<String> existingEndpoints = roleEntries.stream()
                .map(RoleEndpoint::getSecuredEndpointId)
                .collect(Collectors.toSet());


            // delete removed endpoints
            Set<String> toDelete = new HashSet<>(existingEndpoints);
            toDelete.removeAll(request.getSecuredEndpointIds());

            if (!toDelete.isEmpty()) {
                roleEndpointRepository.deleteByRoleIdAndEndpointIds(roleId, new ArrayList<>(toDelete));
            }

            // insert new endpoints
            Set<String> toInsert = new HashSet<>(request.getSecuredEndpointIds());
            toInsert.removeAll(existingEndpoints);

            for (String endpointId : toInsert) {
                RoleEndpoint re = new RoleEndpoint();
                re.setRoleId(roleId);
                re.setRoleName(role.name);
                re.setSecuredEndpointId(endpointId);
                roleEndpointRepository.create(re);
            }
        }

}
