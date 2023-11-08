import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";
import api, { API_PATHS } from "src/services/api";
import { transformUserListApiResponse } from "src/domain/user/user-transformer";
import { UserListApiResponse } from "src/domain/user/user-types";
import { convertQueryValuesToString } from "src/services/api-helper";

async function getUserList(
  params: KlawApiRequestQueryParameters<"showUsers">
): Promise<UserListApiResponse> {
  const queryParams = convertQueryValuesToString({
    ...params,
    ...(params?.teamName && { teamName: params.teamName }),
  });

  return api
    .get<KlawApiResponse<"showUsers">>(
      API_PATHS.showUsers,
      new URLSearchParams(queryParams)
    )
    .then((response) =>
      transformUserListApiResponse({
        apiResponse: response,
        currentPage: Number(params?.pageNo) || 1,
      })
    );
}

export { getUserList };
