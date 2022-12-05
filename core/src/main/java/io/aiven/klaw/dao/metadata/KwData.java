package io.aiven.klaw.dao.metadata;

import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.Topic;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class KwData {
  List<Topic> topics;
  List<Acl> subscriptions;
  List<MessageSchema> schemas;
  List<KwKafkaConnector> kafkaConnectors;
  List<ActivityLog> activityLogs;
}
