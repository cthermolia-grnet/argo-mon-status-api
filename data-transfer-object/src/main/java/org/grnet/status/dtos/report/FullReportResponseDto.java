package org.grnet.status.dtos.report;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "FullReportResponseDto returns the information about the Report object of a tenant")
@Getter
@Setter
public class FullReportResponseDto {

    @Schema(type = SchemaType.STRING,
            description = "Id of the report",
            example = "13a28cec-2940-4fcf-ad95-57fbdaf5bbad")
    public String id;

    @Schema(type = SchemaType.STRING,
            description = "Tenant",
            example = "TENANT")
    public String tenant;

    @Schema(type = SchemaType.BOOLEAN,
            description = "Indicates if the report is disabled",
            example = "false")
    public boolean disabled;

    @Schema(description = "Information about the report")
    public Info info;

    @Schema(description = "Computations settings for the report")
    public Computations computations;

    @Schema(description = "Threshold values")
    public Thresholds thresholds;

    @Schema(description = "Topology schema")
    public TopologySchema topology_schema;

    @Schema(description = "Profiles list")
    public List<Profile> profiles;

    // ✅ CHANGED
    @Schema(description = "Filter tags list")
    public List<FilterTag> filter_tags;


    /* ================= INFO ================= */

    public static class Info {

        @Schema(type = SchemaType.STRING,
                description = "Name of the report",
                example = "CORE")
        public String name;

        @Schema(type = SchemaType.STRING,
                description = "Description of the report",
                example = "Core A/R Report")
        public String description;

        @Schema(type = SchemaType.STRING,
                description = "Creation timestamp",
                example = "2026-01-29 17:52:20")
        public String created;

        @Schema(type = SchemaType.STRING,
                description = "Last updated timestamp",
                example = "2026-01-29 17:52:20")
        public String updated;
    }


    /* ================= COMPUTATIONS ================= */

    public static class Computations {

        @Schema(type = SchemaType.BOOLEAN,
                description = "AR computation enabled",
                example = "true")
        public boolean ar;

        @Schema(type = SchemaType.BOOLEAN,
                description = "Status computation enabled",
                example = "true")
        public boolean status;

        @Schema(description = "List of trend types")
        public List<String> trends;
    }


    /* ================= THRESHOLDS ================= */

    public static class Thresholds {

        @Schema(type = SchemaType.INTEGER,
                description = "Availability threshold",
                example = "80")
        public int availability;

        @Schema(type = SchemaType.INTEGER,
                description = "Reliability threshold",
                example = "90")
        public int reliability;

        @Schema(type = SchemaType.NUMBER,
                format = "double",
                description = "Uptime threshold",
                example = "0.8")
        public double uptime;

        @Schema(type = SchemaType.NUMBER,
                format = "double",
                description = "Unknown threshold",
                example = "0.1")
        public double unknown;

        @Schema(type = SchemaType.NUMBER,
                format = "double",
                description = "Downtime threshold",
                example = "0.1")
        public double downtime;
    }


    /* ================= TOPOLOGY ================= */

    public static class TopologySchema {

        @Schema(description = "Group information")
        public Group group;

        public static class Group {

            @Schema(type = SchemaType.STRING,
                    description = "Type of group",
                    example = "PROJECT")
            public String type;

            @Schema(description = "Nested group info")
            public NestedGroup group;

            public static class NestedGroup {

                @Schema(type = SchemaType.STRING,
                        description = "Type of nested group",
                        example = "SERVICEGROUPS")
                public String type;
            }
        }
    }


    /* ================= PROFILE ================= */

    public static class Profile {

        @Schema(type = SchemaType.STRING,
                description = "Id of the profile",
                example = "1fbb311d-0e9c-4f93-84ac-37207817d708")
        public String id;

        @Schema(type = SchemaType.STRING,
                description = "Name of the profile",
                example = "ARGO_MON")
        public String name;

        @Schema(type = SchemaType.STRING,
                description = "Type of the profile",
                example = "metric")
        public String type;
    }


    /* ================= FILTER TAG ================= */

    // ✅ NEW CLASS
    public static class FilterTag {

        @Schema(type = SchemaType.STRING,
                description = "Tag name",
                example = "scope")
        public String name;

        @Schema(type = SchemaType.STRING,
                description = "Tag value",
                example = "SLA")
        public String value;

        @Schema(type = SchemaType.STRING,
                description = "Tag context",
                example = "argo.group.filter.tags.array")
        public String context;
    }

}
