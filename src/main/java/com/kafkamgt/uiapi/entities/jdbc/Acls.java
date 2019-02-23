package com.kafkamgt.uiapi.entities.jdbc;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name="acls")
public class Acls implements Serializable {

    @Id
    @Column(name = "req_no")
    private String reqNo;

    @Column(name = "topicname")
    private String topicName;

    @Column(name = "env")
    private String environment;

    @Column(name = "teamname")
    private String teamName;

    @Column(name = "consumergroup")
    private String consumerGroup;

    @Column(name = "topictype")
    private String topicType;

    @Column(name = "acl_ip")
    private String acl_ip;

    @Column(name = "acl_ssl")
    private String acl_ssl;
}
