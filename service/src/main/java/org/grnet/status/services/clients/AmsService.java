package org.grnet.status.services.clients;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.dtos.ams.PublishRequest;
import org.grnet.status.services.utils.EncryptUtil;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.dtos.ams.PublishRequest;
import org.grnet.status.services.utils.EncryptUtil;

public interface AmsService {
    void publishMessage(PublishRequest request);
    String getProject();
    String getTopic();
}