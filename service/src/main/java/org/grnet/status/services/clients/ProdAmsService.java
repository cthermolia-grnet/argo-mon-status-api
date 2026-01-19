package org.grnet.status.services.clients;


import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.dtos.ams.PublishRequest;

@ApplicationScoped
@IfBuildProfile("prod")
public class ProdAmsService implements AmsService {

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

    @Override
    public void publishMessage(PublishRequest request) {
        var amsClient = produceClient();
        amsClient.publish(amsToken, project, topic, request);
    }
}

