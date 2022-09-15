package com.kafkamgt.uiapi.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
