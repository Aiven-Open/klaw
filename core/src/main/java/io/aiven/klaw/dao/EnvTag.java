package io.aiven.klaw.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class EnvTag implements Serializable {

  private String id;

  private String name;

  public EnvTag() {}

  public EnvTag(String id, String name) {
    this.id = id;
    this.name = name;
  }
}
