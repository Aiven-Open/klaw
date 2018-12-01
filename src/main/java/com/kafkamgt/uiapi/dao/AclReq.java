package com.kafkamgt.uiapi.dao;


public class AclReq {

    private String req_no;
    private String acl_ip;
    private String acl_ssl;
    private String topicname;
    private String topictype;
    private String consumergroup;
    private String environment;
    private String username;
    private String teamname;
    private String requestingteam;
    private String appname;
    private String remarks;
    private String aclstatus;
    private String requesttime;
    private String requestor;
    private String approver;
    private String approvingtime;

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

    public String getRequestingteam() {
        return requestingteam;
    }

    public void setRequestingteam(String requestingteam) {
        this.requestingteam = requestingteam;
    }

    public String getTopicname() {
        return topicname;
    }

    public void setTopicname(String topicname) {
        this.topicname = topicname;
    }

    public String getReq_no() {
        return req_no;
    }

    public void setReq_no(String req_no) {
        this.req_no = req_no;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAclstatus() {
        return aclstatus;
    }

    public void setAclstatus(String aclstatus) {
        this.aclstatus = aclstatus;
    }

    public String getRequesttime() {
        return requesttime;
    }

    public void setRequesttime(String requesttime) {
        this.requesttime = requesttime;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getApprovingtime() {
        return approvingtime;
    }

    public void setApprovingtime(String approvingtime) {
        this.approvingtime = approvingtime;
    }
}
