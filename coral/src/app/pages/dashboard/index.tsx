import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";

const DashboardPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/index"} />
      <PageHeader title={"Dashboard"} />
      Dashing young lad
    </>
  );
};

export default DashboardPage;
