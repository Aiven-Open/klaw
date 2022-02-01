package com.kafkamgt.uiapi.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class KwRolesPermissionsModel implements Serializable {

    private int id;

    private String roleId;

    private String permission;

    private String description;

    private String rolePermission;

    private String permissionEnabled;
}
