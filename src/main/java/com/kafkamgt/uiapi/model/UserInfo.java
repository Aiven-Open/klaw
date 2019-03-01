package com.kafkamgt.uiapi.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name="users")
public class UserInfo implements Serializable {

    @Id
    @Column(name = "userid")
    private String username;

    @Column(name = "pwd")
    private String pwd;

    @Column(name = "team")
    private String team;

    @Column(name = "roleid")
    private String role;

    @Column(name = "fullname")
    private String fullname;
}
