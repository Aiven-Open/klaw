package io.aiven.klaw.model.response;

import io.aiven.klaw.model.ResourceHistory;
import io.aiven.klaw.model.TopicOverviewInfo;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class TopicOverview extends ResourceOverviewAttributes {
  @NotNull private List<TopicOverviewInfo> topicInfoList;
  private List<AclOverviewInfo> aclInfoList;
  private List<AclOverviewInfo> prefixedAclInfoList;
  private List<AclOverviewInfo> transactionalAclInfoList;
  private List<ResourceHistory> topicHistoryList;
  @NotNull private PromotionStatus topicPromotionDetails;
  @NotNull private List<EnvIdInfo> availableEnvironments;

  private String topicDocumentation;
  @NotNull private Integer topicIdForDocumentation;
}
