package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@IdClass(ActivityLog.class)
@Table(name="kwactivitylog")
public class ActivityLog implements Serializable {

    @Id
    @Column(name = "req_no")
    private String req_no;

    @Column(name = "activityname")
    private String activityName;

    @Column(name = "activitytype")
    private String activityType;

    @Column(name = "activitytime")
    private Timestamp activityTime;

    @Transient
    private String activityTimeString;

    @Column(name = "details")
    private String details;

    @Column(name = "userid")
    private String user;

    @Column(name = "team")
    private String team;

    @Column(name = "env")
    private String env;

    @Transient
    private String totalNoPages;

    @Transient
    private List<String> allPageNos;
}