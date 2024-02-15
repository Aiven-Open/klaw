import { PageHeader } from "@aivenio/aquarium";
import AddNewClusterForm from "src/app/features/configuration/clusters/AddNewClusterForm";

const AddClusterPage = () => {
  return (
    <>
      <PageHeader title={"Add new cluster"} />
      <AddNewClusterForm />
    </>
  );
};

export { AddClusterPage };
