import { ClusterType } from "src/domain/cluster";

type ClusterTypeMap = Record<ClusterType, string>;
const clusterTypeToString: ClusterTypeMap = {
  ALL: "All",
  KAFKA: "Kafka",
  SCHEMA_REGISTRY: "Schema Registry",
  KAFKA_CONNECT: "Kafka Connect",
};

export { clusterTypeToString };
