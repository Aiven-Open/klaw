import { PageHeader } from "@aivenio/aquarium";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import ConnectorRequest from "src/app/features/connectors/request/ConnectorRequest";

const RequestConnector = () => {
  return (
    <AuthenticationRequiredBoundary>
      <>
        <PageHeader title={"Request connector"} />
        <ConnectorRequest />
      </>
    </AuthenticationRequiredBoundary>
  );
};

export default RequestConnector;
