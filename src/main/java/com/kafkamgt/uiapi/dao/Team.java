package com.kafkamgt.uiapi.dao;

public class Team {
    private String teamname;
    private String teammail;
    private String teamphone;
    private String app;
    private String contactperson;
    private String contactpersonmailId;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getContactperson() {
        return contactperson;
    }

    public void setContactperson(String contactperson) {
        this.contactperson = contactperson;
    }

    public String getContactpersonmailId() {
        return contactpersonmailId;
    }

    public void setContactpersonmailId(String contactpersonmailId) {
        this.contactpersonmailId = contactpersonmailId;
    }

    public String getTeamname() {
        return teamname;
    }

    public void setTeamname(String teamname) {
        this.teamname = teamname;
    }

    public String getTeammail() {
        return teammail;
    }

    public void setTeammail(String teammail) {
        this.teammail = teammail;
    }

    public String getTeamphone() {
        return teamphone;
    }

    public void setTeamphone(String teamphone) {
        this.teamphone = teamphone;
    }
}
