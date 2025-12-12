package org.grnet.status.dtos.ams;
import java.util.List;

public class PublishResponse {

    private List<String> messageIds;

    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }
}
