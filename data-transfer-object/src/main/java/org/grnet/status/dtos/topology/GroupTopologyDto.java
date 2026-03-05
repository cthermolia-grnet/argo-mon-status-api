package org.grnet.status.dtos.topology;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Group Topology")
@JsonPropertyOrder({ "date", "group", "type", "subgroup", "tags", "notifications" })

public class GroupTopologyDto {

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
            description = "Subgroup of the topology",
            example = "SubGroupA")
    @JsonProperty("subgroup")
    private String subgroup;


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

    public String getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(String subgroup) {
        this.subgroup = subgroup;
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
                description = "A set of scopes to relate the topology element",
                example = "{TenantA, Report1}")
       @JsonProperty("scope")
       private String scope;

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "Infrastracture",
                example = "Infrastracture A")
        @JsonProperty("infrastructure")
        private String infrastructure;

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "Certification",
                example = "CertificationA")
        @JsonProperty("certification")
        private String certification;

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getInfrastructure() {
            return infrastructure;
        }

        public void setInfrastructure(String infrastructure) {
            this.infrastructure = infrastructure;
        }

        public String getCertification() {
            return certification;
        }

        public void setCertification(String certification) {
            this.certification = certification;
        }
    }
}
