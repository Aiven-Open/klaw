package com.kafkamgt.uiapi.dao;


import java.util.List;

public class Topic {

    private String sequence;
    private String topicname;
    private String topicpartitions;
    private String replicationfactor;
    private String environment;
    private String username;
    private String teamname;
    private String appname;
    private String acl_ip;
    private String acl_ssl;
    private String remarks;
    private String topicstatus;
    private String requesttime;
    private String approvingtime;
    private String topicreqtype;
    private String reqtype;
    private String totalNoPages;
    private List<String> allPageNos;
    private List<String> possibleTeams;

    public String getTopicpartitions() {
        return topicpartitions;
    }

    public void setTopicpartitions(String topicpartitions) {
        this.topicpartitions = topicpartitions;
    }

    public String getReplicationfactor() {
        return replicationfactor;
    }

    public void setReplicationfactor(String replicationfactor) {
        this.replicationfactor = replicationfactor;
    }

    public String getAcl_ip() {
        return acl_ip;
    }

    public void setAcl_ip(String acl_ip) {
        this.acl_ip = acl_ip;
    }

    public String getAcl_ssl() {
        return acl_ssl;
    }

    public void setAcl_ssl(String acl_ssl) {
        this.acl_ssl = acl_ssl;
    }

    public List<String> getPossibleTeams() {
        return possibleTeams;
    }

    public void setPossibleTeams(List<String> possibleTeams) {
        this.possibleTeams = possibleTeams;
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

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getTopicreqtype() {
        return topicreqtype;
    }

    public void setTopicreqtype(String topicreqtype) {
        this.topicreqtype = topicreqtype;
    }

    public String getReqtype() {
        return reqtype;
    }

    public void setReqtype(String reqtype) {
        this.reqtype = reqtype;
    }



    public String getRequesttime() {
        return requesttime;
    }

    public void setRequesttime(String requesttime) {
        this.requesttime = requesttime;
    }

    public String getApprovingtime() {
        return approvingtime;
    }

    public void setApprovingtime(String approvingtime) {
        this.approvingtime = approvingtime;
    }

    public String getTopicstatus() {
        return topicstatus;
    }

    public void setTopicstatus(String topicstatus) {
        this.topicstatus = topicstatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTeamname() {
        return teamname;
    }

    public void setTeamname(String teamname) {
        this.teamname = teamname;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTopicName() {
        return topicname;
    }
    public void setTopicName(String topicname) {
        this.topicname = topicname;
    }
    public String getEnvironment() {
        return environment;
    }
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "Topic [topicName=" + topicname + ", environment=" + environment;
    }



}
