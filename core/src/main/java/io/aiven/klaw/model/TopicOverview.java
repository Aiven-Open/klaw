package io.aiven.klaw.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class TopicOverview {
  List<TopicInfo> topicInfoList;
  List<AclInfo> aclInfoList;
  List<AclInfo> prefixedAclInfoList;
  List<AclInfo> transactionalAclInfoList;
  private List<TopicHistory> topicHistoryList;
  Map<String, String> promotionDetails;
  boolean topicExists;
  List<Map<String, String>> schemaDetails;
  boolean schemaExists;
  boolean prefixAclsExists;
  boolean txnAclsExists;

  String topicDocumentation;
  Integer topicIdForDocumentation;
}
