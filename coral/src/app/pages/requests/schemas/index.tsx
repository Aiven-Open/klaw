import SchemaRequests from "src/app/features/requests/schemas/SchemaRequests";
import PreviewBanner from "src/app/components/PreviewBanner";

const SchemaRequestsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/mySchemaRequests"} />
      <SchemaRequests />
    </>
  );
};

export default SchemaRequestsPage;
