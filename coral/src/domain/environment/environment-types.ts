import { components, operations } from "types/api.d";

type EnvironmentsGetResponse =
  operations["environmentsGet"]["responses"]["200"]["content"]["application/json"];
type EnvironmentDTO = components["schemas"]["Environment"];

type Environment = {
  name: string;
  id: string;
};

const ALL_ENVIRONMENTS_VALUE = "ALL";
const ENVIRONMENT_NOT_INITIALIZED = "d3a914ff-cff6-42d4-988e-b0425128e770";

export type { Environment, EnvironmentDTO, EnvironmentsGetResponse };
export { ALL_ENVIRONMENTS_VALUE, ENVIRONMENT_NOT_INITIALIZED };
