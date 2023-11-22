import { PageHeader } from "@aivenio/aquarium";
import { Navigate, useMatches } from "react-router-dom";
import ApprovalResourceTabs from "src/app/features/approvals/components/ApprovalResourceTabs";
import {
  APPROVALS_TAB_ID_INTO_PATH,
  ApprovalsTabEnum,
  isApprovalsTabEnum,
  APPROVALS_TAB_PATH_LINK_MAP,
} from "src/app/router_utils";
import PreviewBanner from "src/app/components/PreviewBanner";

const ApprovalsPage = () => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const matches = useMatches();
  const currentTab = findMatchingTab(matches);
  if (currentTab === undefined) {
    return <Navigate to={`/approvals/topics`} replace={true} />;
  }

  return (
    <>
      <PreviewBanner linkTarget={APPROVALS_TAB_PATH_LINK_MAP[currentTab]} />
      <PageHeader title={"Approve requests"} />
      <ApprovalResourceTabs currentTab={currentTab} />
    </>
  );

  function findMatchingTab(
    matches: ReturnType<typeof useMatches>
  ): ApprovalsTabEnum | undefined {
    const match = matches
      .map((match) => match.id)
      .find((id) =>
        Object.prototype.hasOwnProperty.call(APPROVALS_TAB_ID_INTO_PATH, id)
      );
    if (isApprovalsTabEnum(match)) {
      return match;
    }
    return undefined;
  }
};

export default ApprovalsPage;
