package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceOverviewAttributes {
  @NotNull boolean topicExists;
  @NotNull boolean prefixAclsExists;
  @NotNull boolean txnAclsExists;
}
