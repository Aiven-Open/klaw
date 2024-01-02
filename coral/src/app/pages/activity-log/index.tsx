import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import ActivityLog from "src/app/features/activity-log/ActivityLog";

const ActivityLogPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/activityLog"} />
      <PageHeader title={"Activity log"} />
      <ActivityLog />
    </>
  );
};

export default ActivityLogPage;
