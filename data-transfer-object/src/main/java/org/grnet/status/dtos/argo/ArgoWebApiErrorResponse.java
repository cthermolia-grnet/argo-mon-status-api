package org.grnet.status.dtos.argo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArgoWebApiErrorResponse {

    public String code;
    public String message;
    public Status status;
    public List<Error> errors;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        public String message;
        public String code;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {
        public String message;
        public String code;
        public String details;
    }

    public String extractMessage() {

        if (message != null && !message.isBlank()) {
            return message;
        }

        if (errors != null && !errors.isEmpty()
                && errors.get(0).details != null
                && !errors.get(0).details.isBlank()) {
            return errors.get(0).details;
        }

        if (status != null && status.message != null && !status.message.isBlank()) {
            return status.message;
        }

        if (errors != null && !errors.isEmpty()
                && errors.get(0).message != null
                && !errors.get(0).message.isBlank()) {
            return errors.get(0).message;
        }

        return "Argo Web Api request failed";
    }
}