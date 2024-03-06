import { ClusterType } from "src/domain/cluster";

type ClusterTypeMap = Record<ClusterType, string>;
const clusterTypeToString: ClusterTypeMap = {
  ALL: "All",
  KAFKA: "Kafka",
  SCHEMA_REGISTRY: "Schema Registry",
  KAFKA_CONNECT: "Kafka Connect",
};

const clusterTypeMapList: { value: ClusterType; name: string }[] = Object.keys(
  clusterTypeToString
).map((clusterTypeKey) => {
  const value = clusterTypeKey as ClusterType;
  return { value, name: clusterTypeToString[value] };
});

export { clusterTypeToString, clusterTypeMapList };
