package io.aiven.klaw.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KwPropertiesModel implements Serializable {

  private String kwKey;

  private String kwValue;

  private String kwDesc;

  @Override
  public String toString() {
    return "KwPropertiesModel{"
        + "kwKey='"
        + kwKey
        + '\''
        + ", kwValue='"
        + kwValue
        + '\''
        + ", kwDesc='"
        + kwDesc
        + '\''
        + '}';
  }
}
