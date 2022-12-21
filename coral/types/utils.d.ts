import type { operations, components } from "types/api.d";

type KlawApiResponse<OperationId extends keyof operations> =
  operations[OperationId]["responses"][200]["content"]["application/json"];
type KlawApiModel<Schema extends keyof components["schemas"]> =
  components["schemas"][Schema];

export type { KlawApiResponse, KlawApiModel };
