import { PageHeader } from "@aivenio/aquarium";
import { useLocation } from "react-router-dom";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import StatusTabs from "src/app/features/approvals/components/StatusTabs";
import Layout from "src/app/layout/Layout";

const ApprovalsPage = () => {
  const { pathname } = useLocation();
  const getEntity = (path: string) => {
    if (path.includes("topics")) {
      return "TOPIC";
    }
    if (path.includes("acl")) {
      return "ACL";
    }
    if (path.includes("schemas")) {
      return "SCHEMA";
    }
    return "CONNECTOR";
  };
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <PageHeader title={"Approve requests"} />
        <StatusTabs entity={getEntity(pathname)} />
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default ApprovalsPage;
