package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="kwavroschemas")
public class MessageSchema implements Serializable {

    @Transient
    private String topicname;

    @Transient
    private String environment;

    @Transient
    private String schemaversion;

    @EmbeddedId
    private MessageSchemaPK messageSchemaPK;

    public String getTopicname() {
        if(this.messageSchemaPK == null)
            return this.topicname;
        else
            return this.messageSchemaPK.getTopicname();
    }

    public void setTopicname(String topicname) {
        this.topicname = topicname;
    }

    public String getEnvironment() {
        if(this.messageSchemaPK == null)
            return this.environment;
        else
            return this.messageSchemaPK.getEnvironment();
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getSchemaversion() {
        if(this.messageSchemaPK == null)
            return this.schemaversion;
        else
            return this.messageSchemaPK.getSchemaversion();
    }

    public void setSchemaversion(String schemaversion) {
        this.schemaversion = schemaversion;
    }

    @Column(name = "teamname")
    private String teamname;

    @Column(name = "schemafull")
    private String schemafull;

}
