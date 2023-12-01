import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";

const ClustersPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/clusters"} />
      <PageHeader title={"Clusters"} />
      <div>View clusters</div>
    </>
  );
};

export { ClustersPage };
