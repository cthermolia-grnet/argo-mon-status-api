package org.grnet.status.dtos.tenant.webapi;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "TenantWebApiGetResponse", description = "Represents the response of a tenant.")
public class TenantWebApiGetResponse {

    private Status status;
    private List<Data> data;   // <-- MUST BE A LIST

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    // Nested classes
    public static class Status {
        private String message;
        private String code;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class Data {
        private String id;
        private Info info;
        private List<DbConf> db_conf;
        private Topology topology;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Info getInfo() {
            return info;
        }

        public void setInfo(Info info) {
            this.info = info;
        }

        public List<DbConf> getDb_conf() {
            return db_conf;
        }

        public void setDb_conf(List<DbConf> db_conf) {
            this.db_conf = db_conf;
        }

        public Topology getTopology() {
            return topology;
        }

        public void setTopology(Topology topology) {
            this.topology = topology;
        }

    }

    public static class Info {
        private String name;
        private String email;
        private String description;
        private String image;
        private String website;
        private String created;
        private String updated;

        // getters/setters omitted for brevity

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getUpdated() {
            return updated;
        }

        public void setUpdated(String updated) {
            this.updated = updated;
        }
    }

    public static class DbConf {
        private String store;
        private String server;
        private int port;
        private String username;
        private String password;

        // getters/setters
    }

    public static class Topology {
        private String type;
        private String feed;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFeed() {
            return feed;
        }

        public void setFeed(String feed) {
            this.feed = feed;
        }

        // getters/setters
    }

}

