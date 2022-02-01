package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@ToString
@Getter
@Setter
@Entity
@IdClass(TopicRequestID.class)
@Table(name="kwtopicrequests")
public class TopicRequest implements Serializable {

    @Id
    @Column(name = "topicid")
    private Integer topicid;

    @Id
    @Column(name = "tenantid")
    private Integer tenantId;

    @Column(name = "topicname")
    private String topicname;

    @Column(name = "env")
    private String environment;

    @Transient
    private String environmentName;

    @Column(name = "partitions")
    private Integer topicpartitions;

    @Column(name = "replicationfactor")
    private String replicationfactor;

    @Column(name = "teamid")
    private Integer teamId;

    @Column(name = "appname")
    private String appname;

    @Column(name = "topictype")
    private String topictype;

    @Column(name = "requestor")
    private String requestor;

    @Column(name = "requesttime")
    private Timestamp requesttime;

    @Transient
    private String requesttimestring;

    @Column(name = "topicstatus")
    private String topicstatus;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "aclip")
    private String acl_ip;

    @Column(name = "aclssl")
    private String acl_ssl;

    @Column(name = "approver")
    private String approver;

    @Column(name = "exectime")
    private Timestamp approvingtime;

    @Column(name = "otherparams")
    private String otherParams;

    @Column(name = "description")
    private String description;

    @Transient
    private String sequence;

    @Transient
    private String history;

    @Transient
    private String username;

    @Transient
    private String totalNoPages;

    @Transient
    private String currentPage;

    @Transient
    private List<String> allPageNos;

    @Transient
    private List<String> possibleTeams;
}