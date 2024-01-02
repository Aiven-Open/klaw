import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";

const ActivityLogPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/activityLog"} />
      <PageHeader title={"Activity log"} />
      <div>Loglog</div>
    </>
  );
};

export default ActivityLogPage;
