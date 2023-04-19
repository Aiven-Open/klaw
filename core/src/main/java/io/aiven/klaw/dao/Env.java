package io.aiven.klaw.dao;

import io.aiven.klaw.helpers.EnvParamsConverter;
import io.aiven.klaw.helpers.EnvTagConverter;
import io.aiven.klaw.model.response.EnvParams;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@IdClass(EnvID.class)
@Table(name = "kwenv")
public class Env implements Serializable {

  @Id
  @Column(name = "id")
  private String id;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Column(name = "envname")
  private String name;

  @Column(name = "stretchcode")
  private String stretchCode;

  @Column(name = "clusterid")
  private Integer clusterId;

  @Column(name = "envtype")
  private String type;

  @Column(name = "otherparams")
  private String otherParams;

  @Column(name = "envexists")
  private String envExists;

  @Column(name = "envstatus")
  private String envStatus;

  @Convert(converter = EnvTagConverter.class)
  @Column(name = "associatedenv")
  private EnvTag associatedEnv;

  @Convert(converter = EnvParamsConverter.class)
  @Column(name = "envparams")
  private EnvParams params;
}
