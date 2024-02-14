import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";

type ClusterInfoFromEnvironment = KlawApiModel<"ClusterInfo">;

type ClusterDetails = KlawApiModel<"KwClustersModelResponse">;

type ClustersPaginatedApiResponse = ResolveIntersectionTypes<
  Paginated<ClusterDetails[]>
>;

type AddNewClusterPayload = KlawApiModel<"KwClustersModel">;

export type {
  ClusterInfoFromEnvironment,
  ClusterDetails,
  ClustersPaginatedApiResponse,
  AddNewClusterPayload,
};
