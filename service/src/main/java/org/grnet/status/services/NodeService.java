package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.grnet.status.dtos.tenant.node.WebApiNodeAvailabilityResponse;
import org.grnet.status.dtos.tenant.node.WebApiNodeStatusResponse;
import org.grnet.status.dtos.tenant.node.WebApiNodeSummaryResponse;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.clients.WebApiService;

@ApplicationScoped
public class NodeService {

    @Inject
    TenantRepository tenantRepository;

    @Inject
    WebApiService webApiService;

    /**
     * Retrieves status information for a node's services.
     *
     * @param nodeName    the node name
     * @param startTime optional start time (W3C format)
     * @param endTime   optional end time (W3C format)
     * @param history   optional flag to include full status history
     * @return status results for the node's services
     */
    public WebApiNodeStatusResponse getStatusByNodeName(String nodeName, String item, String startTime, String endTime, Boolean history) {

        checkIfNodeExist(nodeName);

        return webApiService.retrieveNodeStatus(nodeName, item, startTime, endTime, history);
    }

    /**
     * Retrieves the summary capability for the specified tenant and service.
     *
     * @param nodeName    the node name
     * @param item service name to examine
     * @param startDate   start date
     * @param endDate     end date
     * @param granularity result granularity
     * @return node summary response
     */
    public WebApiNodeSummaryResponse getSummaryByNodeName(String nodeName, String item, String startDate, String endDate, String granularity) {

        checkIfNodeExist(nodeName);

        return webApiService.retrieveNodeSummary(nodeName, item, startDate, endDate, granularity);
    }


    /**
     * Retrieves availability metrics for a node's services.
     *
     * @param nodeName    the node name
     * @param date        optional specific date (YYYY-MM-DD)
     * @param startTime   optional start time (W3C format)
     * @param endTime     optional end time (W3C format)
     * @param startDate   optional start date (YYYY-MM-DD)
     * @param endDate     optional end date (YYYY-MM-DD)
     * @param granularity optional aggregation level (daily or monthly)
     * @return availability results for the node's services
     */
    public WebApiNodeAvailabilityResponse getAvailabilityByNodeName(String nodeName, String item, String date, String startTime, String endTime, String startDate, String endDate, String granularity) {

        checkIfNodeExist(nodeName);

        return webApiService.retrieveNodeAvailability(nodeName, item, date, startTime, endTime, startDate, endDate, granularity);
    }


    private void checkIfNodeExist(String nodeName) {

        tenantRepository.findTenantByNameOptional(nodeName)
                .orElseThrow(() -> new WebApplicationException(
                        "Fetching Node... " +
                         "Node with name '" + nodeName + "' does not exist.", 404
                ));
    }
}
