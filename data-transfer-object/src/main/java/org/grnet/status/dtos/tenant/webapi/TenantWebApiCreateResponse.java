package org.grnet.status.dtos.tenant.webapi;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.Status;

@Schema(name = "TenantWebApiResponse", description = "Represents the response of a tenant.")
public class TenantWebApiCreateResponse {

        private Status status;
        private Data data;

        // Getters and Setters
        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public static class Data {
            private String id;
            private Links links;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public Links getLinks() {
                return links;
            }

            public void setLinks(Links links) {
                this.links = links;
            }
        }

        public static class Links {
            private String self;

            public String getSelf() {
                return self;
            }

            public void setSelf(String self) {
                this.self = self;
            }
        }
    }

