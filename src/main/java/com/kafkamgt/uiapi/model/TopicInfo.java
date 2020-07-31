package com.kafkamgt.uiapi.model;

import lombok.Data;

import java.util.List;

@Data
public class TopicInfo {
    private String sequence;
    private String totalNoPages;
    private List<String> allPageNos;
    private String topicName;
    private String noOfPartitions;
    private String noOfReplcias;
    private String teamname;
    private String cluster;
    private List<String> environmentsList;
}
