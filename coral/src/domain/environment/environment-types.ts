import { KlawApiModel } from "types/utils";

type Environment = {
  name: string;
  id: string;
};
type ClusterInfo = KlawApiModel<"EnvironmentGetClusterInfoResponse">;

const ALL_ENVIRONMENTS_VALUE = "ALL";
const ENVIRONMENT_NOT_INITIALIZED = "d3a914ff-cff6-42d4-988e-b0425128e770";

export type { Environment, ClusterInfo };
export { ALL_ENVIRONMENTS_VALUE, ENVIRONMENT_NOT_INITIALIZED };
