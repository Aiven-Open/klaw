import { KlawApiResponse } from "types/utils";
import { UserListApiResponse } from "src/domain/user/user-types";

function transformUserListApiResponse({
  apiResponse,
  currentPage,
}: {
  apiResponse: KlawApiResponse<"showUsers">;
  currentPage: number;
}): UserListApiResponse {
  if (apiResponse.length === 0) {
    return {
      totalPages: 0,
      currentPage,
      entries: [],
    };
  }

  return {
    totalPages: Number(apiResponse[0].totalNoPages),
    currentPage,
    entries: apiResponse,
  };
}

export { transformUserListApiResponse };
