package org.grnet.status.dtos.tenant.node;

import java.util.List;

public class WebApiNodeUptimeResponse {

    public List<Data> data;

    public static class Data {
        public String name;
        public List<Result> results;
    }

    public static class Result {
        public String date;
        public String uptime;
    }
}