package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
