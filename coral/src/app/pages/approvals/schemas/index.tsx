import PreviewBanner from "src/app/components/PreviewBanner";
import SchemaApprovals from "src/app/features/approvals/schemas/SchemaApprovals";

const SchemaApprovalsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/execSchemas"} />
      <SchemaApprovals />
    </>
  );
};

export default SchemaApprovalsPage;
