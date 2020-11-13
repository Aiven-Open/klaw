package com.kafkamgt.uiapi.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserInfoModel implements Serializable {

    private String username;
    private String team;
    private String role;
    private String fullname;
    private String mailid;
}
