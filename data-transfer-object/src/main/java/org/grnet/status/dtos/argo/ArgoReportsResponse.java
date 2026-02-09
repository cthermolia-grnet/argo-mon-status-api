package org.grnet.status.dtos.argo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

import java.util.List;

public class ArgoReportsResponse {

    @JsonProperty("data")
    public List<ReportItem> data;

    public static class ReportItem {

        @JsonProperty("id")
        public String id;

        @JsonProperty("tenant")
        public String tenant;

        @JsonProperty("info")
        public Info info;
    }

    public static class Info {

        @JsonProperty("name")
        public String name;

        @JsonProperty("description")
        public String description;


        @JsonProperty("created")
        public Instant created;

        @JsonProperty("updated")
        public Instant updated;

        @JsonProperty("disabled")
        public boolean disabled;
    }
}
