package org.grnet.status.dtos.argo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ArgoReportsResponse {

    @JsonProperty("data")
    public List<ReportItem> data;

    public static class ReportItem {
        @JsonProperty("id")
        public String id;

        @JsonProperty("info")
        public Info info;
    }

    public static class Info {

        @JsonProperty("name")
        public String name;
        @JsonProperty("description")
        public String description;

    }
}
