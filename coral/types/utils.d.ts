import type { operations, components } from "types/api.d";

type KlawApiResponse<OperationId extends keyof operations> =
  ResolveIntersectionTypes<
    operations[OperationId]["responses"][200]["content"]["application/json"]
  >;
type KlawApiModel<Schema extends keyof components["schemas"]> =
  components["schemas"][Schema];
type KlawApiRequest<OperationId extends keyof operations> =
  operations[OperationId]["requestBody"]["content"]["application/json"];
type KlawApiRequestQueryParameters<OperationId extends keyof operations> =
  operations[OperationId]["parameters"]["query"];

type ResolveIntersectionTypes<T> = {
  [K in keyof T]: T[K];
  // eslint-disable-next-line @typescript-eslint/ban-types
} & {};

export type {
  KlawApiResponse,
  KlawApiModel,
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  ResolveIntersectionTypes,
};
