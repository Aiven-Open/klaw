package com.kafkamgt.uiapi.entities;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="topic_requests")
public class TopicRequest implements Serializable {

    @Id
    @Column(name = "topicname")
    private String topicname;

    @Id
    @Column(name = "env")
    private String environment;

    @Column(name = "partitions")
    private String topicpartitions;

    @Column(name = "replicationfactor")
    private String replicationfactor;

    @Column(name = "teamname")
    private String teamname;

    @Column(name = "appname")
    private String appname;

    @Column(name = "topictype")
    private String topictype;

    @Column(name = "requestor")
    private String requestor;

    @Column(name = "requesttime")
    private String requesttime;

    @Column(name = "topicstatus")
    private String topicstatus;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "acl_ip")
    private String acl_ip;

    @Column(name = "acl_ssl")
    private String acl_ssl;

    @Column(name = "approver")
    private String approver;

    @Column(name = "exectime")
    private Timestamp approvingtime;

    @Transient
    private String sequence;

    @Transient
    private String username;

    @Transient
    private String totalNoPages;

    @Transient
    private List<String> allPageNos;

    @Transient
    private List<String> possibleTeams;

}