package org.grnet.status.dtos.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "DBConfDto", description = "Represents the configuration of the db_conf in the tenant in the web api.")
@Getter
@Setter
public class DBConfDto {


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "store",
            example = "ar"
    )
    @JsonProperty("store")
    public String store;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "server",
            example = "mongo-test.priv"
    )
    @JsonProperty("server")
    public String server;

    @Schema(
            type = SchemaType.INTEGER,
            implementation = Integer.class,
            description = "port",
            example = "80"
    )
    @JsonProperty("port")
    public Integer port;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "database",
            example = "test_db"
    )
    @JsonProperty("database")
    public String database;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "username",
            example = "admin"
    )
    @JsonProperty("username")
    public String username;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "password",
            example = "admin"
    )
    @JsonProperty("password")
    public String password;

}