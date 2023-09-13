import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import SchemaRegistryEnvironments from "src/app/features/configuration/environments/SchemaRegistry/SchemaRegistryEnvironments";

const SchemaRegistryEnvironmentsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/envs"} />
      <PageHeader title={"Schema Registry Environments"} />
      <SchemaRegistryEnvironments />
    </>
  );
};

export default SchemaRegistryEnvironmentsPage;
