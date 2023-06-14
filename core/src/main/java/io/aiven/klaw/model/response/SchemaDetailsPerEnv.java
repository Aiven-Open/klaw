package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchemaDetailsPerEnv {
  @NotNull private int id;

  @NotNull private int version;

  @NotNull private int nextVersion;

  @NotNull private int prevVersion;

  @NotNull private String compatibility;

  @NotNull private String content;

  @NotNull private String env;

  @NotNull private boolean showNext;

  @NotNull private boolean showPrev;

  @NotNull private boolean latest;
}
