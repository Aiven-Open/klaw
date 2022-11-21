import { Environment } from "src/domain/environment/environment-types";

function createMockEnvironmentDTO(name: Environment) {
  return {
    id: "1",
    name,
    type: "kafka",
    tenantId: 101,
    topicprefix: null,
    topicsuffix: null,
    clusterId: 1,
    tenantName: "default",
    clusterName: "DEV",
    envStatus: "ONLINE",
    otherParams:
      "default.partitions=2,max.partitions=2,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=",
    defaultPartitions: null,
    maxPartitions: null,
    defaultReplicationFactor: null,
    maxReplicationFactor: null,
    showDeleteEnv: false,
    totalNoPages: null,
    allPageNos: null,
  };
}

export { createMockEnvironmentDTO };
