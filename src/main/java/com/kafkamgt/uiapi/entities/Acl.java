package com.kafkamgt.uiapi.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@IdClass(Acl.class)
@Table(name="acls")
public class Acl implements Serializable {

    @Id
    @Column(name = "req_no")
    private String req_no;

    @Column(name = "topicname")
    private String topicname;

    @Column(name = "env")
    private String environment;

    @Column(name = "teamname")
    private String teamname;

    @Column(name = "consumergroup")
    private String consumergroup;

    @Column(name = "topictype")
    private String topictype;

    @Column(name = "acl_ip")
    private String acl_ip;

    @Column(name = "acl_ssl")
    private String acl_ssl;
}
