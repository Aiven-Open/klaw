import { transformPaginatedClustersApiResponse } from "src/domain/cluster/cluster-api-transformer";
import {
  ClustersPaginatedApiResponse,
  AddNewClusterPayload,
} from "src/domain/cluster/cluster-types";
import { Environment } from "src/domain/environment";
import api, { API_PATHS } from "src/services/api";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";

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
  clusterType,
}: KlawApiRequestQueryParameters<"getClustersPaginated">): Promise<ClustersPaginatedApiResponse> {
  const params: KlawApiRequestQueryParameters<"getClustersPaginated"> = {
    clusterType,
    pageNo,
    ...(searchClusterParam && { searchClusterParam: searchClusterParam }),
  };
  const response = await api.get<KlawApiResponse<"getClustersPaginated">>(
    API_PATHS.getClustersPaginated,
    new URLSearchParams(params)
  );

  return transformPaginatedClustersApiResponse(response);
}

async function addNewCluster(
  payload: AddNewClusterPayload
): Promise<KlawApiResponse<"addNewCluster">> {
  const response = await api.post<
    KlawApiResponse<"addNewCluster">,
    AddNewClusterPayload
  >(API_PATHS.addNewCluster, payload);

  return response;
}

export {
  addNewCluster,
  getClusterDetails,
  getClusterInfoFromEnvironment,
  getClustersPaginated,
};
