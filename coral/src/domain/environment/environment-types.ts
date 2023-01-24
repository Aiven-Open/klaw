import { KlawApiModel } from "types/utils";

type Environment = {
  name: KlawApiModel<"Environment">["name"];
  id: KlawApiModel<"Environment">["id"];
  defaultPartitions: number | undefined;
  defaultReplicationFactor: number | undefined;
  maxPartitions: number | undefined;
  maxReplicationFactor: number | undefined;
  topicNamePrefix: string | undefined;
  topicNameSuffix: string | undefined;
  type: KlawApiModel<"Environment">["type"];
};
type ClusterInfo = KlawApiModel<"environmentGetClusterInfoResponse">;

const ALL_ENVIRONMENTS_VALUE = "ALL";
const ENVIRONMENT_NOT_INITIALIZED = "d3a914ff-cff6-42d4-988e-b0425128e770";

export type { Environment, ClusterInfo };
export { ALL_ENVIRONMENTS_VALUE, ENVIRONMENT_NOT_INITIALIZED };
