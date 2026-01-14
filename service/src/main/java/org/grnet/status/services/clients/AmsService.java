package org.grnet.status.services.clients;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.dtos.ams.PublishRequest;
import org.grnet.status.services.utils.EncryptUtil;

@ApplicationScoped
public class AmsService {
//    @Inject
//    EncryptUtil encryptUtil;
    @ConfigProperty(name = "publisher.ams.secret")
    String amsToken;
    @ConfigProperty(name = "ams.url")
    String ams;
    @Inject
    AmsClientFactory amsClientFactory;

    @Getter
    @ConfigProperty(name = "ams.project")
    String project;

    @Getter
    @ConfigProperty(name = "ams.topic")
    String topic;

    private AmsClient produceClient() {
        return amsClientFactory.buildClient(ams);
    }

    public void publishMessage(PublishRequest request) {
        var amsClient = produceClient();
        amsClient.publish(amsToken, project, topic, request);
    }
}
