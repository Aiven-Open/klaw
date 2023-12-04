import { Box, Icon } from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";
import { useQuery } from "@tanstack/react-query";
import { createContext, ReactNode, useContext } from "react";
import { BasePage } from "src/app/layout/page/BasePage";
import { getRequestsWaitingForApproval } from "src/domain/requests/requests-api";
import { RequestsWaitingForApprovalWithTotal } from "src/domain/requests/requests-types";

/** We don't do Authentication on Corals side
 * at the moment, so we only have a AuthUser
 * in the context
 * */
const PendingRequestsContext =
  createContext<RequestsWaitingForApprovalWithTotal>({
    TOPIC: 0,
    ACL: 0,
    SCHEMA: 0,
    CONNECTOR: 0,
    OPERATIONAL: 0,
    USER: 0,
    TOTAL: 0,
  });

const usePendingRequestsContext = () => useContext(PendingRequestsContext);

const PendingRequestsProvider = ({ children }: { children: ReactNode }) => {
  const { data, isLoading } = useQuery(["getRequestsWaitingForApproval"], {
    queryFn: getRequestsWaitingForApproval,
  });

  if (!isLoading && data !== undefined) {
    return (
      <PendingRequestsContext.Provider value={data}>
        {children}
      </PendingRequestsContext.Provider>
    );
  }

  return (
    <BasePage
      content={
        <Box paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
          <div className={"visually-hidden"}>Loading Klaw</div>
          <Icon icon={loading} fontSize={"30px"} />
        </Box>
      }
    />
  );
};

export { PendingRequestsProvider, usePendingRequestsContext };
