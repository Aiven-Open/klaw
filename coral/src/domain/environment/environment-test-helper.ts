import { Environment } from "src/domain/environment/environment-types";
import { KlawApiModel } from "types/utils";

const defaultEnvironmentDTO: KlawApiModel<"EnvModelResponse"> = {
  id: "1",
  name: "DEV",
  type: "kafka",
  tenantId: 101,
  clusterId: 1,
  tenantName: "default",
  clusterName: "DEV",
  envStatus: "ONLINE",
  otherParams:
    "default.partitions=2,max.partitions=2,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=",
  showDeleteEnv: false,
  totalNoPages: "1",
  allPageNos: ["1"],
  associatedEnv: undefined,
  topicprefix: undefined,
  topicsuffix: undefined,
  defaultPartitions: undefined,
  maxPartitions: undefined,
  defaultReplicationFactor: undefined,
  maxReplicationFactor: undefined,
  clusterType: "ALL",
  params: {},
};

function createMockEnvironmentDTO(
  environment: Partial<KlawApiModel<"EnvModelResponse">>
): KlawApiModel<"EnvModelResponse"> {
  return { ...defaultEnvironmentDTO, ...environment };
}

const defaultEnvironment: Environment = {
  name: "DEV",
  id: "1",
  defaultPartitions: undefined,
  defaultReplicationFactor: undefined,
  maxPartitions: undefined,
  maxReplicationFactor: undefined,
  topicNamePrefix: undefined,
  topicNameSuffix: undefined,
  type: "kafka",
};

function createEnvironment(environment: Partial<Environment>): Environment {
  return { ...defaultEnvironment, ...environment };
}

export { createMockEnvironmentDTO, createEnvironment };
