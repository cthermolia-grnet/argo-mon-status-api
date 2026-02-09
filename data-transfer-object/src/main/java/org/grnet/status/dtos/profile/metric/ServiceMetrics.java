package org.grnet.status.dtos.profile.metric;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceMetrics {

    private String service;
    private List<String> metrics;
}
