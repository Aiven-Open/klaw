package com.kafkamgt.uiapi.dao;

import java.util.List;

public class TopicInfo {
    private String sequence;
    private String totalNoPages;
    private List<String> allPageNos;
    private String topicName;
    private String noOfPartitions;
    private String noOfReplcias;
    private String teamname;

    public String getTeamname() {
        return teamname;
    }

    public void setTeamname(String teamname) {
        this.teamname = teamname;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getTotalNoPages() {
        return totalNoPages;
    }

    public void setTotalNoPages(String totalNoPages) {
        this.totalNoPages = totalNoPages;
    }

    public List<String> getAllPageNos() {
        return allPageNos;
    }

    public void setAllPageNos(List<String> allPageNos) {
        this.allPageNos = allPageNos;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getNoOfPartitions() {
        return noOfPartitions;
    }

    public void setNoOfPartitions(String noOfPartitions) {
        this.noOfPartitions = noOfPartitions;
    }

    public String getNoOfReplcias() {
        return noOfReplcias;
    }

    public void setNoOfReplcias(String noOfReplcias) {
        this.noOfReplcias = noOfReplcias;
    }
}
