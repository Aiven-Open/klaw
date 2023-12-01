import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import { Clusters } from "src/app/features/configuration/clusters/Clusters";

const ClustersPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/clusters"} />
      <PageHeader title={"Clusters"} />
      <Clusters />
    </>
  );
};

export { ClustersPage };
