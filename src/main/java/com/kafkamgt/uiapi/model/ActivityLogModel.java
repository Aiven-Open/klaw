package com.kafkamgt.uiapi.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
public class ActivityLogModel implements Serializable {

    private Integer req_no;

    private String activityName;

    private String activityType;

    private Timestamp activityTime;

    private String activityTimeString;

    private String details;

    private String user;

    private String team;

    private String env;

    private String totalNoPages;

    private List<String> allPageNos;

}