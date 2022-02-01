package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;

@ToString
@Getter
@Setter
@Entity
@Table(name="kwregisterusers")
public class RegisterUserInfo implements Serializable {

    @Id
    @Column(name = "userid")
    private String username;

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

    @Column(name = "status")
    private String status;

    @Column(name = "registeredtime")
    private Timestamp registeredTime;

    @Column(name = "approver")
    private String approver;

    @Column(name = "registrationid")
    private String registrationId;

    @Column(name = "tenantid")
    private int tenantId;

    @Transient
    private String team;
}
