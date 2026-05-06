package org.grnet.status.dtos.topology;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.grnet.status.dtos.Status;

import java.util.List;

public class WebApiFeedsTopologyResponse {

    @JsonProperty("status")
    public Status status;

    @JsonProperty("data")
    public List<FeedTopologyDto> data;
}
