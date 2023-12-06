import { Environment } from "src/domain/environment";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";
import api, { API_PATHS } from "src/services/api";
import { ClustersPaginatedApiResponse } from "src/domain/cluster/cluster-types";
import { transformPaginatedClustersApiResponse } from "src/domain/cluster/cluster-api-transformer";

const getClusterInfoFromEnvironment = async ({
  envSelected,
  envType,
}: {
  envSelected: string;
  envType: Environment["type"];
}): Promise<KlawApiResponse<"getClusterInfoFromEnv">> => {
  const params = new URLSearchParams({ envSelected, envType });
  return api.get<KlawApiResponse<"getClusterInfoFromEnv">>(
    API_PATHS.getClusterInfoFromEnv,
    params
  );
};

function getClusterDetails(clusterId: string) {
  const params = new URLSearchParams({ clusterId });
  return api.get<KlawApiResponse<"getClusterDetails">>(
    API_PATHS.getClusterDetails,
    params
  );
}

async function getClustersPaginated({
  pageNo,
  searchClusterParam,
}: Omit<
  KlawApiRequestQueryParameters<"getClustersPaginated">,
  "clusterType"
>): Promise<ClustersPaginatedApiResponse> {
  const params: KlawApiRequestQueryParameters<"getClustersPaginated"> = {
    clusterType: "all",
    pageNo,
    ...(searchClusterParam && { searchClusterParam: searchClusterParam }),
  };
  const response = await api.get<KlawApiResponse<"getClustersPaginated">>(
    API_PATHS.getClustersPaginated,
    new URLSearchParams(params)
  );

  return transformPaginatedClustersApiResponse(response);
}

export {
  getClusterInfoFromEnvironment,
  getClusterDetails,
  getClustersPaginated,
};
