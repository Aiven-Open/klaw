package com.kafkamgt.uiapi.dao;


public class SchemaRequest {

    private String topicname;
    private String environment;
    private String username;
    private String teamname;
    private String appname;
    private String schemafull;
    private String remarks;
    private String topicstatus;
    private String requesttime;
    private String approvingtime;
    private String schemaversion;

    public String getSchemaversion() {
        return schemaversion;
    }

    public void setSchemaversion(String schemaversion) {
        this.schemaversion = schemaversion;
    }

    public String getTopicname() {
        return topicname;
    }

    public void setTopicname(String topicname) {
        this.topicname = topicname;
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

    public String getSchemafull() {
        return schemafull;
    }

    public void setSchemafull(String schemafull) {
        this.schemafull = schemafull;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTopicstatus() {
        return topicstatus;
    }

    public void setTopicstatus(String topicstatus) {
        this.topicstatus = topicstatus;
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
}
