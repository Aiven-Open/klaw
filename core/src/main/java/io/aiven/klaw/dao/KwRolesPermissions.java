package io.aiven.klaw.dao;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

@ToString
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@IdClass(KwRolesPermissionsID.class)
@Table(name = "kwrolespermissions")
public class KwRolesPermissions implements Serializable {

  @Id
  @Column(name = "id")
  private int id;

  @Id
  @Column(name = "tenantid")
  private int tenantId;

  @Column(name = "roleid")
  private String roleId;

  @Column(name = "permission")
  private String permission;

  @Column(name = "description")
  private String description;
}
