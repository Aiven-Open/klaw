package com.kafkamgt.uiapi.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
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
    private String aclip;

    @Column(name = "acl_ssl")
    private String aclssl;

    @Override
    public String toString() {
        return "Acl{" +
                "req_no='" + req_no + '\'' +
                ", topicname='" + topicname + '\'' +
                ", environment='" + environment + '\'' +
                ", teamname='" + teamname + '\'' +
                ", consumergroup='" + consumergroup + '\'' +
                ", topictype='" + topictype + '\'' +
                ", aclip='" + aclip + '\'' +
                ", aclssl='" + aclssl + '\'' +
                '}';
    }
}
