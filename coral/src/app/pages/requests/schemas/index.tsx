import { SchemaRequests } from "src/app/features/requests/schemas/SchemaRequests";
import PreviewBanner from "src/app/components/PreviewBanner";
import Layout from "src/app/layout/Layout";

const SchemaRequestsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/mySchemaRequests"} />
      <SchemaRequests />
    </>
  );
};

export default SchemaRequestsPage;
