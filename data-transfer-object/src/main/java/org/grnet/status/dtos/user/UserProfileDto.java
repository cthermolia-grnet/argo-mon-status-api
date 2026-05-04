package org.grnet.status.dtos.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.UserGroupInfoDto;

import java.util.List;

@Schema(description = "Request to fetch reports from a target API")
public class UserProfileDto {

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "Unique identifier for the user.",
                example = "24329fb1b49c7fc0aa668d07410d4857a685c1d365976e42823368faa27442e7@aai.eosc-portal.eu"
        )
        @JsonProperty("id")
        public String id;

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "User's username",
                example = "username"
        )
        @JsonProperty("username")
        public String username;

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "User's email",
                example = "foo@grnet.gr"
        )
        @JsonProperty("email")
        public String email;

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "User's name",
                example = "foo"
        )
        @JsonProperty("name")
        public String name;

        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "User's Surname",
                example = "foo"
        )
        @JsonProperty("surname")
        public String surname;


        @Schema(type = SchemaType.STRING,
                implementation = String.class,
                description = "User's uid",
                example = "uid"
        )
        @JsonProperty("uid")
        public String uid;

        @Schema(
                description = "User entitlements (group memberships and roles)"
        )
        @JsonProperty("groups")
        public List<UserGroupInfoDto> groups;
}
