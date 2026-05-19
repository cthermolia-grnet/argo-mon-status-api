package org.grnet.status.dtos.tenant.webapi;

import java.util.List;

public class TenantWebApiGroupStatusResponse {

    public List<GroupStatusData> data;

    public static class GroupStatusData {

        public String name;

        public List<GroupStatusResult> results;
    }

    public static class GroupStatusResult {

        public String timestamp;

        public String value;
    }
}
