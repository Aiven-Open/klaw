import { getEnvironments } from "src/domain/environment/environment-api";
import { mockGetEnvironments } from "src/domain/environment/environment-api.msw";
import {
  Environment,
  EnvironmentDTO,
} from "src/domain/environment/environment-types";

export { getEnvironments, mockGetEnvironments };
export type { Environment, EnvironmentDTO };
