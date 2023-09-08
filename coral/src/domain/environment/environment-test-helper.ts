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
  envStatusTime: "2023-09-08T12:34:10.615919098",
  otherParams:
    "default.partitions=2,max.partitions=2,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=",
  showDeleteEnv: false,
  totalNoPages: "1",
  allPageNos: ["1"],
  associatedEnv: undefined,
  clusterType: "ALL",
  params: {
    applyRegex: undefined,
    defaultPartitions: undefined,
    defaultRepFactor: undefined,
    maxPartitions: undefined,
    maxRepFactor: undefined,
    topicPrefix: undefined,
    topicSuffix: undefined,
  },
};

function createMockEnvironmentDTO(
  environment: Partial<KlawApiModel<"EnvModelResponse">>
): KlawApiModel<"EnvModelResponse"> {
  return { ...defaultEnvironmentDTO, ...environment };
}

const defaultEnvironment: Environment = {
  name: "DEV",
  id: "1",
  params: {},
  type: "kafka",
};

function createEnvironment(environment: Partial<Environment>): Environment {
  return { ...defaultEnvironment, ...environment };
}

export { createMockEnvironmentDTO, createEnvironment };
