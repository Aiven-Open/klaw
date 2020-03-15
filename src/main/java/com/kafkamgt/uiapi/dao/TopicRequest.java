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
@Table(name="topic_requests")
public class TopicRequest implements Serializable {

    @Transient
    private String topicname;

    @Transient
    private String environment;

    public String getTopicname() {
        if(this.topicRequestPK == null)
            return this.topicname;
        else
            return this.topicRequestPK.getTopicname();
    }

    public void setTopicname(String topicname) {
        this.topicname = topicname;
    }

    public String getEnvironment() {
        if(this.topicRequestPK == null)
            return this.environment;
        else
            return this.topicRequestPK.getEnvironment();
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @EmbeddedId
    private TopicRequestPK topicRequestPK;

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
    private Timestamp requesttime;

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