import { RequestEntityType } from "src/domain/requests/requests-types";
import { KlawApiResponse } from "types/utils";

type RequestsWaitingForApproval = {
  [key in RequestEntityType]: number;
};

const getRequestsWaitingForApprovalTransformer = (
  data: KlawApiResponse<"getRequestStatistics">
) => {
  const { requestEntityStatistics } = data;

  let requestsWaitingForApproval: RequestsWaitingForApproval = {
    TOPIC: 0,
    ACL: 0,
    SCHEMA: 0,
    CONNECTOR: 0,
    USER: 0,
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

    requestsWaitingForApproval = {
      ...requestsWaitingForApproval,
      [requestEntityType]: requestStatusCountSet.find(
        (requestStatusCountSet) =>
          requestStatusCountSet.requestStatus === "CREATED"
      )?.count,
    };
  });

  return requestsWaitingForApproval;
};

export { getRequestsWaitingForApprovalTransformer };
