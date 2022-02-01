package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name="kwusers")
public class UserInfo implements Serializable {

    @Id
    @Column(name = "userid")
    private String username;

    @Column(name = "tenantid")
    private int tenantId;

    @Column(name = "pwd")
    private String pwd;

    @Column(name = "teamid")
    private Integer teamId;

    @Column(name = "roleid")
    private String role;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "mailid")
    private String mailid;

    @Column(name = "otherparams")
    private String otherParams;

}
