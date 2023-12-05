import { useQuery } from "@tanstack/react-query";
import { getRequestsWaitingForApproval } from "src/domain/requests/requests-api";

export const usePendingRequests = () => {
  const { data, isLoading } = useQuery(["getRequestsWaitingForApproval"], {
    queryFn: getRequestsWaitingForApproval,
  });

  if (!isLoading && data !== undefined) {
    return data;
  }

  return {
    TOPIC: 0,
    ACL: 0,
    SCHEMA: 0,
    CONNECTOR: 0,
    OPERATIONAL: 0,
    USER: 0,
    TOTAL: 0,
  };
};
