package org.grnet.status.dtos.topology;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Endpoint Topology")
@JsonPropertyOrder({ "date", "group", "type", "service", "hostname", "tags", "notifications" })

public class EndpointTopologyDto {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Date of the topology",
            example = "2026-03-03")
    @JsonProperty("date")
    private String date;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Group of the topology",
            example = "GroupA")
    @JsonProperty("group")
    private String group;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Type of the topology",
            example = "Type1")
    @JsonProperty("type")
    private String type;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Service of the topology",
            example = "ServiceA")
    @JsonProperty("service")
    private String service;


    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Hostname of the topology",
            example = "HostnameA")
    @JsonProperty("hostname")
    private String hostname;


    private NotificationsDTO notifications;

    private TagsDTO tags;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public NotificationsDTO getNotifications() {
        return notifications;
    }

    public void setNotifications(NotificationsDTO notifications) {
        this.notifications = notifications;
    }

    public TagsDTO getTags() {
        return tags;
    }

    public void setTags(TagsDTO tags) {
        this.tags = tags;
    }

    public static class NotificationsDTO {


        @Schema(
                type = SchemaType.ARRAY,
                implementation = String.class,
                description = "A set of contact emails",
                example = "[\"johndoe@foo.com\",\"jsmith@foo.com\"]"
        )
        @JsonProperty("contacts")
        private List<String> contacts;

        @Schema(type = SchemaType.BOOLEAN,
                implementation = Boolean.class,
                description = "A flag to define if contacts will be notified or not",
                example = "true")
        @JsonProperty("enabled")
        private boolean enabled;

        public List<String> getContacts() {
            return contacts;
        }

        public void setContacts(List<String> contacts) {
            this.contacts = contacts;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class TagsDTO {

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "info id",
                example = "1")
        @JsonProperty("info_ID")

        private String info_ID;


        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "Flag to define if the element is to production or not",
                example = "1")
        @JsonProperty("production")
        private String production;

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "Flag to define that topology element is monitored or not",
                example = "1")
        @JsonProperty("monitored")

        private String monitored;

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "A set of scopes to relate the topology element",
                example = "{TenantA, Report1}")
        @JsonProperty("scope")
        private String scope;

        public String getMonitored() {
            return monitored;
        }

        public void setMonitored(String monitored) {
            this.monitored = monitored;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getInfo_ID() {
            return info_ID;
        }

        public void setInfo_ID(String info_ID) {
            this.info_ID = info_ID;
        }

        public String getProduction() {
            return production;
        }

        public void setProduction(String production) {
            this.production = production;
        }
    }
}

