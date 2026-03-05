package org.grnet.status.dtos.topology;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.lang.reflect.Array;

@JsonPropertyOrder({ "date", "name", "title", "description", "tags" })
public class ServiceTypeDto {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the service type",
            example = "Service_Type_A")
    @JsonProperty("name")
    private String name;


    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Title of the service type",
            example = "Service_Type_A")
    @JsonProperty("title")
    private String title;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Description of the service type",
            example = "This is a service type")
    @JsonProperty("description")
    private String description;

    @Schema(
            type = SchemaType.ARRAY,
            implementation = String.class,
            description = "Tags of the service type",
            example = "[\"special-service\", \"beta\"]"
    )
    @JsonProperty("tags")
    private String[] tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}