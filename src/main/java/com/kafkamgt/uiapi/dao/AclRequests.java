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
@Table(name="acl_requests")
public class AclRequests implements Serializable {

    @Id
    @Column(name = "req_no")
    private String req_no;

    @Column(name = "topicname")
    private String topicname;

    @Column(name = "env")
    private String environment;

    @Column(name = "teamname")
    private String teamname;

    @Column(name = "requestingteam")
    private String requestingteam;

    @Column(name = "appname")
    private String appname;

    @Column(name = "topictype")
    private String topictype;

    @Column(name = "consumergroup")
    private String consumergroup;

    @Column(name = "requestor")
    private String username;

    @Column(name = "requesttime")
    private Timestamp requesttime;

    @Column(name = "topicstatus")
    private String aclstatus;

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
    private String totalNoPages;

    @Transient
    private List<String> allPageNos;

}
