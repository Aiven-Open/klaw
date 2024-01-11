import { useToast } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";
import { getRequestsWaitingForApproval } from "src/domain/requests";
import { parseErrorMsg } from "src/services/mutation-utils";

export const usePendingRequests = () => {
  const { data, isLoading, isError, error } = useQuery(
    ["getRequestsWaitingForApproval"],
    {
      queryFn: getRequestsWaitingForApproval,
    }
  );

  const toast = useToast();

  useEffect(() => {
    if (isError) {
      toast({
        message: `Could not fetch pending requests: ${parseErrorMsg(error)}`,
        position: "bottom-left",
        variant: "default",
      });
    }
  }, [isError]);

  if (isLoading || data === undefined) {
    return {
      TOPIC: undefined,
      ACL: undefined,
      SCHEMA: undefined,
      CONNECTOR: undefined,
      OPERATIONAL: undefined,
      USER: undefined,
      TOTAL_NOTIFICATIONS: undefined,
    };
  }

  return data;
};
