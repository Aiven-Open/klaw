package io.aiven.klaw.dao.metadata;

import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.TopicRequest;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class KwRequests {
  List<TopicRequest> topicRequests;
  List<AclRequests> subscriptionRequests;
  List<SchemaRequest> schemaRequests;
  List<KafkaConnectorRequest> connectorRequests;
  List<RegisterUserInfo> userRequests;
  List<ActivityLog> activityLogs;
}
