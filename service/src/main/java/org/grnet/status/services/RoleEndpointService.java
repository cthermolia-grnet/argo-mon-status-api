package org.grnet.status.services;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.repositories.RoleEndpointRepository;
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

        var roleEndpoints = roleEndpointRepository.findAll();

        var grouped = roleEndpoints.stream()
                .collect(Collectors.groupingBy(RoleEndpoint::getRoleId));

        var assignments = grouped.entrySet().stream()
                .map(entry -> {
                    var group = entry.getValue();
                    var first = group.get(0);

                    var ra = new RoleEndpointAssignmentResponse.RoleAssignment();

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

        var response = new RoleEndpointAssignmentResponse();
        response.setAssignments(assignments);

        return response;
    }

    public RoleEndpointAssignmentResponse getAssignedEndpointsByRoleId(String roleId) {

        var roleEndpoints = roleEndpointRepository.findAll()
                .stream()
                .filter(re -> re.getRoleId().equals(roleId))
                .toList();

        var response = new RoleEndpointAssignmentResponse();

        if (roleEndpoints.isEmpty()) {
            response.setAssignments(Collections.emptyList());
            return response;
        }

        var first = roleEndpoints.get(0);

        var assignment = new RoleEndpointAssignmentResponse.RoleAssignment();

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

        var existing = roleEndpointRepository.findAll();

        var requestedRoleIds = request.getAssignments()
                .stream()
                .map(RoleEndpointAssignmentRequest.RoleAssignment::getRoleId)
                .collect(Collectors.toSet());

        var dbRoleIds = existing.stream()
                .map(RoleEndpoint::getRoleId)
                .collect(Collectors.toSet());

        // DELETE roles not in request
        var rolesToDelete = new HashSet<>(dbRoleIds);
        rolesToDelete.removeAll(requestedRoleIds);

        for (String roleId : rolesToDelete) {
            roleEndpointRepository.deleteByRoleId(roleId);
        }

        //  SYNC remaining roles
        for (var assignment : request.getAssignments()) {

            var roleId = assignment.getRoleId();
            var roleName = assignment.getRoleName();

            var incoming = new HashSet<>(assignment.getSecuredEndpointIds());

            var roleEntries = roleEndpointRepository.list("role_id", roleId);

            var existingEndpoints = roleEntries.stream()
                    .map(RoleEndpoint::getSecuredEndpointId)
                    .collect(Collectors.toSet());

            // delete removed endpoints
            var toDelete = new HashSet<>(existingEndpoints);
            toDelete.removeAll(incoming);

            if (!toDelete.isEmpty()) {
                roleEndpointRepository.deleteByRoleIdAndEndpointIds(roleId, new ArrayList<>(toDelete));
            }

            // insert new endpoints
            var toInsert = new HashSet<>(incoming);
            toInsert.removeAll(existingEndpoints);

            for (String endpointId : toInsert) {
                var re = new RoleEndpoint();
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
        var role = roleResponse.stream()
                .filter(r -> r.id.equals(roleId))
                .findFirst()
                .orElse(null);

        var roleEntries = roleEndpointRepository.list("role_id",roleId);
        var existingEndpoints = roleEntries.stream()
                .map(RoleEndpoint::getSecuredEndpointId)
                .collect(Collectors.toSet());

            // delete removed endpoints
        var toDelete = new HashSet<>(existingEndpoints);
        toDelete.removeAll(request.getSecuredEndpointIds());

        if (!toDelete.isEmpty()) {
            roleEndpointRepository.deleteByRoleIdAndEndpointIds(roleId, new ArrayList<>(toDelete));
        }

        // insert new endpoints
        var toInsert = new HashSet<>(request.getSecuredEndpointIds());
        toInsert.removeAll(existingEndpoints);

        for (String endpointId : toInsert) {
            var re = new RoleEndpoint();
            re.setRoleId(roleId);
            re.setRoleName(role.name);
            re.setSecuredEndpointId(endpointId);
            roleEndpointRepository.create(re);
        }
    }
}
