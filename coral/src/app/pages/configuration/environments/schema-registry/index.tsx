import { PageHeader } from "@aivenio/aquarium";
import SchemaRegistryEnvironments from "src/app/features/configuration/environments/SchemaRegistry/SchemaRegistryEnvironments";

const SchemaRegistryEnvironmentsPage = () => {
  return (
    <>
      <PageHeader title={"Schema Registry Environments"} />
      <SchemaRegistryEnvironments />
    </>
  );
};

export default SchemaRegistryEnvironmentsPage;
