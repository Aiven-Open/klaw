package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceOverviewAttributes {
  @NotNull boolean topicExists;
  @NotNull boolean schemaExists;
  @NotNull boolean prefixAclsExists;
  @NotNull boolean txnAclsExists;
  // Indicates if this schema env is restricted to only allow new schemas through promotion.
  @NotNull private boolean createSchemaAllowed;
}
