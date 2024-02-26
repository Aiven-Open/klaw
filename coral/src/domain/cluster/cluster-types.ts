import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";

type ClusterInfoFromEnvironment = KlawApiModel<"ClusterInfo">;

type ClusterDetails = KlawApiModel<"KwClustersModelResponse">;

type ClustersPaginatedApiResponse = ResolveIntersectionTypes<
  Paginated<ClusterDetails[]>
>;

type AddNewClusterPayload = KlawApiModel<"KwClustersModel">;

type ClusterKafkaFlavor =
  KlawApiModel<"KwClustersModelResponse">["kafkaFlavor"];

type ClusterType = KlawApiModel<"KwClustersModelResponse">["clusterType"];

export type {
  ClusterInfoFromEnvironment,
  ClusterDetails,
  ClustersPaginatedApiResponse,
  AddNewClusterPayload,
  ClusterKafkaFlavor,
  ClusterType,
};
