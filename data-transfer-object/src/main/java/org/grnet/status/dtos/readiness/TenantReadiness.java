package org.grnet.status.dtos.readiness;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class TenantReadiness {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;
    @JsonProperty("ready")
    private boolean ready;

    @JsonProperty("last_check")
    private String lastCheck;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = ReadinessInfo.class,
            description = "Data Readiness Info "
    )
    @JsonProperty("data")
    @Valid
    public ReadinessInfo data;


    @Schema(
            type = SchemaType.OBJECT,
            implementation = ReadinessInfo.class,
            description = "Topology Readiness Info "
    )
    @JsonProperty("topology")
    @Valid
    public ReadinessInfo topology;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = ReadinessInfo.class,
            description = "Reports Readiness Info "
    )
    @JsonProperty("reports")
    @Valid
    public ReadinessInfo reports;



    public TenantReadiness() {


    }

    // Getters & Setters

    public String getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(String lastCheck) {
        this.lastCheck = lastCheck;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public ReadinessInfo getData() {
        return data;
    }

    public void setData(ReadinessInfo data) {
        this.data = data;
    }

    public ReadinessInfo getTopology() {
        return topology;
    }

    public void setTopology(ReadinessInfo topology) {
        this.topology = topology;
    }

    public ReadinessInfo getReports() {
        return reports;
    }

    public void setReports(ReadinessInfo reports) {
        this.reports = reports;
    }

    public class ReadinessInfo {

        @JsonProperty("ready")
        private boolean ready;

        @JsonProperty("message")
        @JsonInclude(JsonInclude.Include.NON_NULL)

        private String message;

        public boolean isReady() {
            return ready;
        }

        public void setReady(boolean ready) {
            this.ready = ready;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
