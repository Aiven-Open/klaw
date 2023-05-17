package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class SchemaDetailsPerEnv {
  private int id;

  private int version;

  private int nextVersion;

  private int prevVersion;

  private String compatibility;

  private String content;

  private String env;

  private boolean showNext;

  private boolean showPrev;

  private boolean latest;
}
