package com.kafkamgt.uiapi.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@IdClass(SchemaRequest.class)
@Table(name="schema_requests")
public class SchemaRequest implements Serializable {

    @Transient
    private String topicname;

    @Transient
    private String environment;

    @Transient
    private String schemaversion;

    @EmbeddedId
    private SchemaRequestPK schemaRequestPK;

    public String getTopicname() {
        if(this.schemaRequestPK == null)
            return this.topicname;
        else
            return this.schemaRequestPK.getTopicname();
    }

    public void setTopicname(String topicname) {
        this.topicname = topicname;
    }

    public String getEnvironment() {
        if(this.schemaRequestPK == null)
            return this.environment;
        else
            return this.schemaRequestPK.getEnvironment();
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getSchemaversion() {
        if(this.schemaRequestPK == null)
            return this.schemaversion;
        else
            return this.schemaRequestPK.getSchemaversion();
    }

    public void setSchemaversion(String schemaversion) {
        this.schemaversion = schemaversion;
    }

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
