import { RequestsWaitingForApprovalWithTotal } from "src/domain/requests/requests-types";
import { KlawApiModel, KlawApiResponse } from "types/utils";

const getRequestsWaitingForApprovalTransformer = (
  data: KlawApiModel<"RequestsCountOverview">
) => {
  const { requestEntityStatistics } = data;

  let requestsWaitingForApproval: RequestsWaitingForApprovalWithTotal = {
    TOPIC: 0,
    ACL: 0,
    SCHEMA: 0,
    CONNECTOR: 0,
    USER: 0,
    OPERATIONAL: 0,
    TOTAL_NOTIFICATIONS: 0,
  };

  if (requestEntityStatistics === undefined) {
    return requestsWaitingForApproval;
  }

  requestEntityStatistics.forEach((statistics) => {
    const { requestEntityType, requestStatusCountSet } = statistics;

    if (
      requestEntityType === undefined ||
      requestStatusCountSet === undefined
    ) {
      return;
    }

    const amountOfRequestsForEntity =
      requestStatusCountSet.find(
        (requestStatusCountSet) =>
          requestStatusCountSet.requestStatus === "CREATED"
      )?.count || 0;

    requestsWaitingForApproval = {
      ...requestsWaitingForApproval,
      [requestEntityType]: amountOfRequestsForEntity,
      TOTAL_NOTIFICATIONS:
        // We do not include the USER and OPERATIONAL requests in this total
        // As it is used to display notifications for users, not superadmin
        requestEntityType !== "USER" && requestEntityType !== "OPERATIONAL"
          ? requestsWaitingForApproval.TOTAL_NOTIFICATIONS +
            amountOfRequestsForEntity
          : requestsWaitingForApproval.TOTAL_NOTIFICATIONS,
    };
  });

  return requestsWaitingForApproval;
};

function activityLogTransformer(
  apiResponse: KlawApiResponse<"showActivityLog">
) {
  if (apiResponse.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }

  return {
    totalPages: Number(apiResponse[0].totalNoPages),
    currentPage: Number(apiResponse[0].currentPage),
    entries: apiResponse,
  };
}

export { activityLogTransformer, getRequestsWaitingForApprovalTransformer };
