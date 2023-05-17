import { PageHeader } from "@aivenio/aquarium";
import ConnectorRequest from "src/app/features/connectors/request/ConnectorRequest";

const RequestConnector = () => {
  return (
    <>
      <PageHeader title={"Request connector"} />
      <ConnectorRequest />
    </>
  );
};

export default RequestConnector;
