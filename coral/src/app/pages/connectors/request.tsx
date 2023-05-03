import { PageHeader } from "@aivenio/aquarium";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import ConnectorRequest from "src/app/features/connectors/request/ConnectorRequest";
import LayoutWithoutNav from "src/app/layout/LayoutWithoutNav";

const RequestConnector = () => {
  return (
    <AuthenticationRequiredBoundary>
      <LayoutWithoutNav>
        <PageHeader title={"Request connector"} />
        <ConnectorRequest />
      </LayoutWithoutNav>
    </AuthenticationRequiredBoundary>
  );
};

export default RequestConnector;
