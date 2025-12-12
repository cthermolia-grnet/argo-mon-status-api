package org.grnet.status.util;

import java.util.HashMap;

public class Event {

    private String event;
    private Properties properties;
    private String timestamp;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static class Properties {
        private HashMap<String,String> jobParams=new HashMap<>();

        public HashMap<String, String> getJobParams() {
            return jobParams;
        }

        public void setJobParams(HashMap<String, String> jobParams) {
            this.jobParams = jobParams;
        }
    }
}