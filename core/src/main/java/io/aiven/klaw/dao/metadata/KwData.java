package io.aiven.klaw.dao.metadata;

import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.Topic;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KwData {
  String klawVersion;
  String createdTime;
  List<Topic> topics;
  List<Acl> subscriptions;
  List<MessageSchema> schemas;
  List<KwKafkaConnector> kafkaConnectors;
}
