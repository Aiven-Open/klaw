import { transformUserListApiResponse } from "src/domain/user/user-transformer";
import { UserListApiResponse } from "src/domain/user/user-types";
import api, { API_PATHS } from "src/services/api";
import { convertQueryValuesToString } from "src/services/api-helper";
import {
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
} from "types/utils";

async function getUserList(
  params: KlawApiRequestQueryParameters<"showUsers">
): Promise<UserListApiResponse> {
  const queryParams = convertQueryValuesToString({
    ...params,
    ...(params?.teamName && { teamName: params.teamName }),
    ...(params?.searchUserParam && { searchUserParam: params.searchUserParam }),
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

async function changePassword(
  payload: KlawApiRequest<"changePwd">
): Promise<KlawApiResponse<"changePwd">> {
  return api.post(API_PATHS.changePwd, payload);
}

export { changePassword, getUserList };
