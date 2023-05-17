package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

@Data
public class TopicHistory implements Serializable {
  @NotNull private String environmentName;

  @NotNull private String teamName;

  @NotNull private String requestedBy;

  @NotNull private String requestedTime;

  @NotNull private String approvedBy;

  @NotNull private String approvedTime;

  @NotNull private String remarks;
}
