import { getEnvironments } from "src/domain/environment/environment-api";
import { mockGetEnvironments } from "src/domain/environment/environment-api.msw";
import {
  ALL_ENVIRONMENTS_VALUE,
  Environment,
  EnvironmentDTO,
} from "src/domain/environment/environment-types";

export { getEnvironments, mockGetEnvironments, ALL_ENVIRONMENTS_VALUE };
export type { Environment, EnvironmentDTO };
