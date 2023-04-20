import { PageHeader } from "@aivenio/aquarium";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import ConnectorRequest from "src/app/features/connectors/request/ConnectorRequest";
import Layout from "src/app/layout/Layout";

const RequestConnector = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <PageHeader title={"Request connector"} />
        <ConnectorRequest />
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default RequestConnector;
