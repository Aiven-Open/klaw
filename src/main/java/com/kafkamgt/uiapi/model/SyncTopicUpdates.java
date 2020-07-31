package com.kafkamgt.uiapi.model;

import lombok.Data;

@Data
public class SyncTopicUpdates {
    private String sequence;
    private String req_no;
    private String topicName;
    private String partitions;
    private String replicationFactor;
    private String teamSelected;
    private String envSelected;
}
