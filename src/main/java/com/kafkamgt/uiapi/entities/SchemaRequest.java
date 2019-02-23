package com.kafkamgt.uiapi.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="schema_requests")
public class SchemaRequest implements Serializable {

    @Id
    @Column(name = "topicname")
    private String topicname;

    @Id
    @Column(name = "env")
    private String environment;

    @Id
    @Column(name = "versionschema")
    private String schemaversion;

    @Column(name = "teamname")
    private String teamname;

    @Column(name = "appname")
    private String appname;

    @Column(name = "schemafull")
    private String schemafull;

    @Column(name = "requestor")
    private String username;

    @Column(name = "requesttime")
    private String requesttime;

    @Column(name = "topicstatus")
    private String topicstatus;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "approver")
    private String approver;

    @Column(name = "exectime")
    private Timestamp approvingtime;

}
