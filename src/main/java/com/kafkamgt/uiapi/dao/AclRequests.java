package com.kafkamgt.uiapi.dao;

import com.kafkamgt.uiapi.model.AclIPPrincipleType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
@IdClass(AclRequestID.class)
@Table(name="kwaclrequests")
public class AclRequests implements Serializable {

    @Id
    @Column(name = "aclid")
    private Integer req_no;

    @Id
    @Column(name = "tenantid")
    private Integer tenantId;

    @Column(name = "topicname")
    private String topicname;

    @Column(name = "env")
    private String environment;

    @Transient
    private String environmentName;

    @Column(name = "teamid")
    private Integer teamId;

    @Column(name = "requestingteam")
    private Integer requestingteam;

    @Column(name = "appname")
    private String appname;

    // Producer Consumer
    @Column(name = "topictype")
    private String topictype;

    @Column(name = "consumergroup")
    private String consumergroup;

    @Column(name = "requestor")
    private String username;

    @Column(name = "requesttime")
    private Timestamp requesttime;

    @Transient
    private String requesttimestring;

    @Column(name = "topicstatus")
    private String aclstatus;

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

    // create or delete
    @Column(name = "acltype")
    private String aclType;

    // prefixed acls or Literal(default)
    @Column(name = "aclpatterntype")
    private String aclPatternType;

    // Topic
    @Column(name = "aclresourcetype")
    private String aclResourceType;

    // TransactionalID
    @Column(name = "transactionalid")
    private String transactionalId;

    @Column(name = "otherparams")
    private String otherParams;

    @Column(name = "jsonparams")
    private String jsonParams;

    @Column(name = "aclipprincipletype")
    private AclIPPrincipleType aclIpPrincipleType;

    @Transient
    private String totalNoPages;

    @Transient
    private String currentPage;

    @Transient
    private List<String> allPageNos;
}
