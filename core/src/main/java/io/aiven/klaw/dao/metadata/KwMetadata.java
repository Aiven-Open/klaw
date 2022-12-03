package io.aiven.klaw.dao.metadata;

import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KwMetadata {
  KwAdminConfig kwAdminConfig;
  KwData kwData;
  KwRequests KwRequests;
}

@Builder
@Getter
@Setter
class KwData {
  List<Topic> topics;
  List<Acl> subscriptions;
  List<MessageSchema> schemas;
  List<KwKafkaConnector> kafkaConnectors;
  List<ActivityLog> activityLogs;
}

class KwRequests {
  List<TopicRequest> topicRequests;
  List<AclRequests> aclRequests;
  List<SchemaRequest> schemaRequests;
  List<KafkaConnectorRequest> connectorRequests;
  List<RegisterUserInfo> userRequests;
}
