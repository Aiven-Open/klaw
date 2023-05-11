import { PageHeader } from "@aivenio/aquarium";
import { Navigate, useMatches } from "react-router-dom";
import RequestsResourceTabs from "src/app/features/requests/RequestsResourceTabs";
import {
  REQUESTS_TAB_ID_INTO_PATH,
  RequestsTabEnum,
  isRequestsTabEnum,
} from "src/app/router_utils";

const RequestsPage = () => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const matches = useMatches();
  const currentTab = findMatchingTab(matches);
  if (currentTab === undefined) {
    return <Navigate to={`/requests/topics`} replace={true} />;
  }
  return (
    <>
      <PageHeader title={"My team's requests"} />
      <RequestsResourceTabs currentTab={currentTab} />
    </>
  );

  function findMatchingTab(
    matches: ReturnType<typeof useMatches>
  ): RequestsTabEnum | undefined {
    const match = matches
      .map((match) => match.id)
      .find((id) =>
        Object.prototype.hasOwnProperty.call(REQUESTS_TAB_ID_INTO_PATH, id)
      );
    if (isRequestsTabEnum(match)) {
      return match;
    }
    return undefined;
  }
};

export default RequestsPage;
