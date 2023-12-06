import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";

type ClusterInfoFromEnvironment = KlawApiModel<"ClusterInfo">;

type ClusterDetails = KlawApiModel<"KwClustersModelResponse">;

type ClustersPaginatedApiResponse = ResolveIntersectionTypes<
  Paginated<ClusterDetails[]>
>;

export type {
  ClusterInfoFromEnvironment,
  ClusterDetails,
  ClustersPaginatedApiResponse,
};
