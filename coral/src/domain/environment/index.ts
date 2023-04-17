import {
  getEnvironments,
  getSchemaRegistryEnvironments,
  getSyncConnectorsEnvironments,
} from "src/domain/environment/environment-api";
import { mockGetEnvironments } from "src/domain/environment/environment-api.msw";
import {
  ALL_ENVIRONMENTS_VALUE,
  Environment,
} from "src/domain/environment/environment-types";

export {
  getEnvironments,
  mockGetEnvironments,
  ALL_ENVIRONMENTS_VALUE,
  getSchemaRegistryEnvironments,
  getSyncConnectorsEnvironments,
};
export type { Environment };
