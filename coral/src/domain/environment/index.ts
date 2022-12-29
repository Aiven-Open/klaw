import { getEnvironments } from "src/domain/environment/environment-api";
import { mockGetEnvironments } from "src/domain/environment/environment-api.msw";
import { clusterInfoFromEnvironment } from "src/domain/environment/environment-queries";
import {
  ALL_ENVIRONMENTS_VALUE,
  ClusterInfo,
  Environment,
} from "src/domain/environment/environment-types";

export {
  getEnvironments,
  mockGetEnvironments,
  ALL_ENVIRONMENTS_VALUE,
  clusterInfoFromEnvironment,
};
export type { Environment, ClusterInfo };
