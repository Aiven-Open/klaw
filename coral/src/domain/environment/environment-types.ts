import { KlawApiModel, KlawApiResponse } from "types/utils";

//@TODO this seems to be specifically modified to fit
// for creating a TopicRequest, we should check this
// with backend and align API and our types/needs
type Environment = {
  name: KlawApiModel<"EnvModel">["name"];
  id: KlawApiModel<"EnvModel">["id"];
  defaultPartitions: number | undefined;
  defaultReplicationFactor: number | undefined;
  maxPartitions: number | undefined;
  maxReplicationFactor: number | undefined;
  topicNamePrefix: string | undefined;
  topicNameSuffix: string | undefined;
  type: KlawApiModel<"EnvModel">["type"];
};
// getClusterInfoFromEnv inline object, not a model
type ClusterInfo = KlawApiResponse<"getClusterInfoFromEnv">;

const ALL_ENVIRONMENTS_VALUE = "ALL";
const ENVIRONMENT_NOT_INITIALIZED = "d3a914ff-cff6-42d4-988e-b0425128e770";

export type { Environment, ClusterInfo };
export { ALL_ENVIRONMENTS_VALUE, ENVIRONMENT_NOT_INITIALIZED };
