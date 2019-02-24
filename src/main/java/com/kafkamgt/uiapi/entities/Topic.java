package com.kafkamgt.uiapi.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@IdClass(Topic.class)
@Table(name="topics")
public class Topic implements Serializable {

    @Transient
    private String topicname;

    @Transient
    private String environment;

    @EmbeddedId
    private TopicPK topicPK;

    public String getTopicname() {
        if(this.topicPK == null)
            return this.topicname;
        else
            return this.topicPK.getTopicname();
    }

    public void setTopicname(String topicname) {
        this.topicname = topicname;
    }

    public String getEnvironment() {
        if(this.topicPK == null)
            return this.environment;
        else
            return this.topicPK.getEnvironment();
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Column(name = "teamname")
    private String teamname;

    @Column(name = "appname")
    private String appname;
}
