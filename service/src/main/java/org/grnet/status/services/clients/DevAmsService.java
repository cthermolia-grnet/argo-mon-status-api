package org.grnet.status.services.clients;

import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.dtos.ams.PublishRequest;

@ApplicationScoped
@UnlessBuildProfile("prod")
public class DevAmsService implements AmsService {

    @Override
    public void publishMessage(PublishRequest request) {
        System.out.println(" [DEV MODE] Skipping AMS publish. Payload:  + request");
        Log.info(" [DEV MODE] Skipping AMS publish. Payload: " + request);
    }

    @Override
    public String getProject() {
        return "DEV_PROJECT";
    }

    @Override
    public String getTopic() {
        return "DEV_TOPIC";
    }
}
