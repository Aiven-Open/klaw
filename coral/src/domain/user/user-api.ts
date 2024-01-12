import {
  KlawApiModel,
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
} from "types/utils";
import api, { API_PATHS } from "src/services/api";
import { transformUserListApiResponse } from "src/domain/user/user-transformer";
import { User, UserListApiResponse } from "src/domain/user/user-types";
import { convertQueryValuesToString } from "src/services/api-helper";

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

async function getUser(): Promise<User> {
  return api.get<KlawApiResponse<"getMyProfileInfo">>(
    API_PATHS.getMyProfileInfo
  );
}

async function changePassword(
  payload: KlawApiRequest<"changePwd">
): Promise<KlawApiResponse<"changePwd">> {
  return api.post(API_PATHS.changePwd, payload);
}

async function updateProfile(profile: KlawApiModel<"ProfileModel">) {
  return api.post<
    KlawApiResponse<"updateProfile">,
    KlawApiRequest<"updateProfile">
  >(API_PATHS.updateProfile, profile);
}

async function getMyTenantInfo(): Promise<KlawApiResponse<"getMyTenantInfo">> {
  return api.get(API_PATHS.getMyTenantInfo);
}

async function login(payload: { username: string; password: string }) {
  return api.login(payload);
}

export {
  changePassword,
  getUserList,
  updateProfile,
  getMyTenantInfo,
  getUser,
  login,
};
