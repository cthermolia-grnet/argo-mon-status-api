package org.grnet.status.dtos.tenant;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "UserDto", description = "Represents the configuration of a user existing in tenant in the web api.")

public class UserDto {


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "id",
            example = "11d8c193-8a9f-45a9-a441-8db1739b9334"
    )
    @JsonProperty("id")
    @Valid
    @NotNull(message = "id can not be null")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            description = "Users 's name",
            example = "Joe Doe"
    )
    @NotNull(message = "Name must not be null")
    @NotBlank(message = "Name must not be empty")
    @JsonProperty("name")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            description = "Users's email",
            example = "joedoe@gmail.com"
    )
    @NotNull(message = "Email must not be null")
    @NotBlank(message = "Email must not be empty")
    @JsonProperty("email")
    public String email;


    @Schema(
            type = SchemaType.STRING,
            description = "Users 's api key",
            example = "8aa5a7d4953c5b478dda68513188e3e0c8b5ed8r"
    )
    @NotNull(message = "Api key must not be null")
    @NotBlank(message = "Api key must not be empty")
    @JsonProperty("api_key")
    public String api_key;


    @Schema(
            type = SchemaType.ARRAY,
            description = "Users 's roles",
            example = "["+"admin"+"]"
    )
    @NotEmpty(message = "Roles can not be null")
    @JsonProperty("roles")
    public List<String> roles;
}
