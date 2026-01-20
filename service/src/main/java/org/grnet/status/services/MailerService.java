package org.grnet.status.services;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.enums.InvitationStatus;
import org.grnet.status.enums.MailType;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class MailerService {

    private static final Logger LOG = Logger.getLogger(MailerService.class);

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "api.server.url")
    String serviceUrl;

    @ConfigProperty(name = "quarkus.mailer.from")
    String contactMail;

    @ConfigProperty(name = "api.mail.title", defaultValue = "ARGO MON Status")
    String title;

    @Inject
    @Location("tenant_invitation.html")
    Template tenantInvitationTemplate;

    @Inject
    @Location("tenant_invitation_response_notify_user.html")
    Template tenantInvitationAcceptTemplate;

    @Inject
    @Location("tenant_invitation_response_notify_admin.html")
    Template tenantInvitationNotifyAdminTemplate;


    public void sendTenantInvitationEmail(List<String> recipientEmail,
                                          String tenantName,
                                          String role,
                                          String invitationUrl) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("contactMail", contactMail);
        params.put("title", title);

        var resolvedLogo = serviceUrl + "/v1/images/logo.png";
        params.put("logoUrl", resolvedLogo);

        params.put("tenantName", tenantName);
        params.put("role", role);
        params.put("invitationUrl", invitationUrl);

        sendBcc(tenantInvitationTemplate, params, MailType.TENANT_INVITATION_CREATED, recipientEmail);
    }


    public void sendInvitationAcceptedToInvitee(List<String> recipientEmail,
                                                String tenantName,
                                                String role, String uiBaseUrl) {

        HashMap<String, Object> params = new HashMap<>();
        var resolvedLogo = serviceUrl + "/v1/images/logo.png";
        params.put("logoUrl", resolvedLogo);

        params.put("contactMail", contactMail);
        params.put("title", title);
        params.put("tenantName", tenantName);
        params.put("role", role);
        params.put("uiUrl", uiBaseUrl);


        sendBcc(tenantInvitationAcceptTemplate, params, MailType.TENANT_INVITATION_RESPONSE_NOTIFY_USER, recipientEmail);
    }

    public void sendInvitationResponseToAdmins(List<String> adminEmails,
                                               String tenantName,
                                               String inviteeEmail,
                                               String role,
                                               InvitationStatus status,
                                               String uiBaseUrl) {

        HashMap<String, Object> params = new HashMap<>();

        var resolvedLogo = serviceUrl + "/v1/images/logo.png";
        params.put("logoUrl", resolvedLogo);

        params.put("contactMail", contactMail);
        params.put("title", title);
        params.put("tenantName", tenantName);
        params.put("inviteeEmail", inviteeEmail);
        params.put("role", role);
        params.put("status", status.name());
        params.put("uiUrl", uiBaseUrl);

        sendBcc(tenantInvitationNotifyAdminTemplate, params, MailType.TENANT_INVITATION_RESPONSE_NOTIFY_ADMIN, adminEmails);
    }

    private void sendBcc(Template template,
                         HashMap<String, Object> params,
                         MailType mailType,
                         List<String> recipients) {

        var mailTemplate = mailType.execute(template, params);

        var mail = new Mail();
        mail.setHtml(mailTemplate.getBody());
        mail.setSubject(mailTemplate.getSubject());
        mail.setBcc(recipients);

        try {
            mailer.send(mail);
            LOG.infof("Invitation email sent to %s (subject=%s)", recipients, mail.getSubject());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send invitation email to %s", recipients);
        }
    }
}
