package com.kafkamgt.uiapi.dao;

import java.util.List;

public class PCStream {
    String topicName;
    String env;
    List<String> producerTeams;
    List<String> consumerTeams;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public List<String> getProducerTeams() {
        return producerTeams;
    }

    public void setProducerTeams(List<String> producerTeams) {
        this.producerTeams = producerTeams;
    }

    public List<String> getConsumerTeams() {
        return consumerTeams;
    }

    public void setConsumerTeams(List<String> consumerTeams) {
        this.consumerTeams = consumerTeams;
    }
}
