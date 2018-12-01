package com.kafkamgt.uiapi.dao;

import java.util.List;

public class AclInfo {

    private String sequence;
    private String req_no;
    private String acl_ip;
    private String acl_ssl;
    private String topicname;
    private String topictype;
    private String consumergroup;
    private String environment;
    private String teamname;
    private String operation;
    private String permission;
    private String totalNoPages;
    private List<String> allPageNos;
    private List<String> possibleTeams;

    public List<String> getPossibleTeams() {
        return possibleTeams;
    }

    public void setPossibleTeams(List<String> possibleTeams) {
        this.possibleTeams = possibleTeams;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getReq_no() {
        return req_no;
    }

    public void setReq_no(String req_no) {
        this.req_no = req_no;
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

    public String getTopicname() {
        return topicname;
    }

    public void setTopicname(String topicname) {
        this.topicname = topicname;
    }

    public String getTopictype() {
        return topictype;
    }

    public void setTopictype(String topictype) {
        this.topictype = topictype;
    }

    public String getConsumergroup() {
        return consumergroup;
    }

    public void setConsumergroup(String consumergroup) {
        this.consumergroup = consumergroup;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getTeamname() {
        return teamname;
    }

    public void setTeamname(String teamname) {
        this.teamname = teamname;
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
}
