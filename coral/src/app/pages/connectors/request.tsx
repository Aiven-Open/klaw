import { PageHeader } from "@aivenio/aquarium";
import ConnectorRequest from "src/app/features/connectors/request/ConnectorRequest";
import Layout from "src/app/layout/Layout";

const RequestConnector = () => {
  return (
    <>
      <PageHeader title={"Request connector"} />
      <ConnectorRequest />
    </>
  );
};

export default RequestConnector;
