package com.kafkamgt.uiapi.dao;

import java.io.Serializable;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@IdClass(KwPropertiesID.class)
@Table(name = "kwproperties")
public class KwProperties implements Serializable {

  @Id
  @Column(name = "kwkey")
  private String kwKey;

  @Id
  @Column(name = "tenantid")
  private int tenantId;

  @Column(name = "kwvalue")
  private String kwValue;

  @Column(name = "kwdesc")
  private String kwDesc;
}
