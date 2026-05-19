package org.grnet.status.dtos.tenant.webapi;

import java.util.List;

public class TenantWebApiGroupResultsResponse {

    public List<GroupResultsData> data;

    public static class GroupResultsData {

        public String name;

        public List<GroupResult> results;
    }

    public static class GroupResult {

        public String date;

        public String availability;

        public String uptime;
    }
}