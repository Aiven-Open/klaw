import { KlawApiModel } from "types/utils";

type ClusterInfoFromEnvironment = KlawApiModel<"ClusterInfo">;

type ClusterDetails = KlawApiModel<"KwClustersModelResponse">;

export type { ClusterInfoFromEnvironment, ClusterDetails };
