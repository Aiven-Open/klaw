import { useQuery } from "@tanstack/react-query";
import { getRequestsWaitingForApproval } from "src/domain/requests";

export const usePendingRequests = () => {
  const { data, isLoading } = useQuery(["getRequestsWaitingForApproval"], {
    queryFn: getRequestsWaitingForApproval,
  });

  if (!isLoading && data !== undefined) {
    return data;
  }

  return {
    TOPIC: undefined,
    ACL: undefined,
    SCHEMA: undefined,
    CONNECTOR: undefined,
    OPERATIONAL: undefined,
    USER: undefined,
    TOTAL_NOTIFICATIONS: undefined,
  };
};
