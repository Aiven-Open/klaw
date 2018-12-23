package com.kafkamgt.uiapi.dao;

import java.util.List;

public class ActivityLog {
    private String activityName;
    private String activityType;
    private String activityTime;
    private String details;
    private String env;
    private String user;
    private String team;
    private String totalNoPages;
    private List<String> allPageNos;

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
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

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(String activityTime) {
        this.activityTime = activityTime;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
