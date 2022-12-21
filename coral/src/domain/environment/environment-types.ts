import type { KlawApiResponse, KlawApiModel } from "types/utils";

type EnvironmentsGetResponse = KlawApiResponse<"environmentsGet">;
type EnvironmentDTO = KlawApiModel<"Environment">;

type Environment = {
  name: string;
  id: string;
};

const ALL_ENVIRONMENTS_VALUE = "ALL";
const ENVIRONMENT_NOT_INITIALIZED = "d3a914ff-cff6-42d4-988e-b0425128e770";

export type { Environment, EnvironmentDTO, EnvironmentsGetResponse };
export { ALL_ENVIRONMENTS_VALUE, ENVIRONMENT_NOT_INITIALIZED };
