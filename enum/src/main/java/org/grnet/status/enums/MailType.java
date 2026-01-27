package org.grnet.status.enums;

import io.quarkus.qute.Template;
import java.util.HashMap;

public enum MailType {

    TENANT_INVITATION_CREATED {
        @Override
        public MailTemplate execute(Template emailTemplate, HashMap<String, Object> templateParams) {

            var body = emailTemplate
                    .data("logoUrl", templateParams.get("logoUrl"))
                    .data("title", templateParams.get("title"))
                    .data("sendFrom", templateParams.get("sendFrom"))
                    .data("replyTo", templateParams.get("replyTo"))

                    .data("recipientName", templateParams.get("recipientName"))
                    .data("tenantName", templateParams.get("tenantName"))
                    .data("role", templateParams.get("role"))
                    .data("invitationUrl", templateParams.get("invitationUrl"))
                    .render();

            var subject = "[" + templateParams.get("title") + "] Invitation to join tenant: " + templateParams.get("tenantName");

            return new MailTemplate(subject, body);
        }
    },

    TENANT_INVITATION_RESPONSE_NOTIFY_USER {
        @Override
        public MailTemplate execute(Template emailTemplate, HashMap<String, Object> templateParams) {

            var body = emailTemplate
                    .data("logoUrl", templateParams.get("logoUrl"))
                    .data("title", templateParams.get("title"))
                    .data("sendFrom", templateParams.get("sendFrom"))
                    .data("replyTo", templateParams.get("replyTo"))

                    .data("tenantName", templateParams.get("tenantName"))
                    .data("role", templateParams.get("role"))
                    .data("uiUrl", templateParams.get("uiUrl"))
                    .render();

            var subject = "[" + templateParams.get("title") + "] Invitation accepted – access granted to " + templateParams.get("tenantName");

            return new MailTemplate(subject, body);
        }
    },

    TENANT_INVITATION_RESPONSE_NOTIFY_ADMIN {
        @Override
        public MailTemplate execute(Template emailTemplate, HashMap<String, Object> templateParams) {

            var body = emailTemplate
                    .data("logoUrl", templateParams.get("logoUrl"))
                    .data("title", templateParams.get("title"))
                    .data("sendFrom", templateParams.get("sendFrom"))
                    .data("replyTo", templateParams.get("replyTo"))

                    .data("tenantName", templateParams.get("tenantName"))
                    .data("inviteeEmail", templateParams.get("inviteeEmail"))
                    .data("role", templateParams.get("role"))
                    .data("status", templateParams.get("status"))
                    .data("uiUrl", templateParams.get("uiUrl"))
                    .render();

            var subject = "[" + templateParams.get("title") +
                    "] Invitation " + templateParams.get("status") +
                    " – " + templateParams.get("tenantName");

            return new MailTemplate(subject, body);
        }
    };

    public abstract MailTemplate execute(Template mailTemplate, HashMap<String, Object> templateParams);

    public static class MailTemplate {
        private String subject;
        private String body;

        public MailTemplate(String subject, String body) {
            this.subject = subject;
            this.body = body;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
