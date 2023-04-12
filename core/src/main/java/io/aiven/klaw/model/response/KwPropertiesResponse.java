package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class KwPropertiesResponse {
  private String result;
  private String kwkey;
  private String kwvalue;
  private String kwdesc;
}
