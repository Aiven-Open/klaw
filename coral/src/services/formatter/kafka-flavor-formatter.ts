import { ClusterKafkaFlavor } from "src/domain/cluster";

type ClusterKafkaFlavorMap = Record<ClusterKafkaFlavor, string>;
const kafkaFlavorToString: ClusterKafkaFlavorMap = {
  APACHE_KAFKA: "Apache Kafka",
  AIVEN_FOR_APACHE_KAFKA: "Aiven for Apache Kafka",
  CONFLUENT: "Confluent",
  CONFLUENT_CLOUD: "Confluent Cloud",
  OTHERS: "others",
};

export { kafkaFlavorToString };
