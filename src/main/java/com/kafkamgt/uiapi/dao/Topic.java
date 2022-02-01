package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@Entity
@IdClass(TopicID.class)
@Table(name="kwtopics")
public class Topic implements Serializable {

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
    private List<String> environmentsList;

    @Column(name = "partitions")
    private Integer noOfPartitions;

    @Column(name = "replicationfactor")
    private String noOfReplcias;

    @Column(name = "teamid")
    private Integer teamId;

    @Column(name = "appname")
    private String appname;

    @Column(name = "otherparams")
    private String otherParams;

    @Column(name = "description")
    private String description;

    @Column(name = "documentation")
    private String documentation;

    @Column(name = "history")
    private String history;

    @Column(name = "jsonparams")
    private String jsonParams;

    @Transient
    private boolean isExistingTopic;

}
