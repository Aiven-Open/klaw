import { PageHeader } from "@aivenio/aquarium";
import { Navigate, useMatches } from "react-router-dom";
import ApprovalResourceTabs from "src/app/features/approvals/components/ApprovalResourceTabs";
import {
  ApprovalsTabEnum,
  APPROVALS_TAB_ID_INTO_PATH,
  isApprovalsTabEnum,
} from "src/app/router_utils";
import Layout from "src/app/layout/Layout";

const ApprovalsPage = () => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const matches = useMatches();
  const currentTab = findMatchingTab(matches);
  if (currentTab === undefined) {
    return <Navigate to={`/approvals/topics`} replace={true} />;
  }
  return (
    <Layout>
      <PageHeader title={"Approve requests"} />
      <ApprovalResourceTabs currentTab={currentTab} />
    </Layout>
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
