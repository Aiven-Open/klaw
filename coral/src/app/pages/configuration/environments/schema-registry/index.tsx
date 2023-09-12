import PreviewBanner from "src/app/components/PreviewBanner";
import SchemaRegistryEnvironments from "src/app/features/configuration/environments/SchemaRegistry/SchemaRegistryEnvironments";

const SchemaRegistryEnvironmentsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/envs"} />
      <SchemaRegistryEnvironments />
    </>
  );
};

export default SchemaRegistryEnvironmentsPage;
