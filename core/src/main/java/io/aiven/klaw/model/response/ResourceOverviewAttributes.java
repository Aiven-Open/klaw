package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class ResourceOverviewAttributes {
  boolean topicExists;
  boolean schemaExists;
  boolean prefixAclsExists;
  boolean txnAclsExists;
}
