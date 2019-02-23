package com.kafkamgt.uiapi.entities.jdbc;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@IdClass(Topics.class)
@Table(name="topics")
public class Topics implements Serializable {

    @Id
    @Column(name = "topicname")
    private String topicName;

    @Id
    @Column(name = "env")
    private String env;

    @Column(name = "teamname")
    private String teamName;

    @Column(name = "appname")
    private String appName;
}
