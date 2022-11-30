import { components, operations } from "types/api.d";

type EnvironmentsGetResponse =
  operations["environmentsGet"]["responses"]["200"]["content"]["application/json"];
type EnvironmentDTO = components["schemas"]["Environment"];

type Environment = {
  name: string;
  id: string;
};

export type { Environment, EnvironmentDTO, EnvironmentsGetResponse };
