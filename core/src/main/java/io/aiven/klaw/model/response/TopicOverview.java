package io.aiven.klaw.model.response;

import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.TopicHistory;
import io.aiven.klaw.model.TopicInfo;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class TopicOverview extends ResourceOverviewAttributes {
  List<TopicInfo> topicInfoList;
  List<AclInfo> aclInfoList;
  List<AclInfo> prefixedAclInfoList;
  List<AclInfo> transactionalAclInfoList;
  private List<TopicHistory> topicHistoryList;
  Map<String, String> topicPromotionDetails;
  List<EnvIdInfo> availableEnvironments;

  String topicDocumentation;
  Integer topicIdForDocumentation;
}
