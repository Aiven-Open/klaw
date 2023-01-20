package io.aiven.klaw.model;

import lombok.Data;

@Data
public class ResourceOverviewAttributes {
  boolean topicExists;
  boolean schemaExists;
  boolean prefixAclsExists;
  boolean txnAclsExists;
}
