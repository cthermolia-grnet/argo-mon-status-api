package org.grnet.status.dtos.tenant.node;

import java.util.List;

public class WebApiNodeSummaryResponse {

    public List<NodeSummaryData> data;

    public static class NodeSummaryData {

        public String name;

        public List<NodeSummaryResult> results;
    }

    public static class NodeSummaryResult {

        public String date;

        public String availability;

        public String uptime;
    }
}