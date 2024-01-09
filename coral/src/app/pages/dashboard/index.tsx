import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import Dashboard from "src/app/features/dashboard/Dashboard";

const DashboardPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/index"} />
      <PageHeader title={"Dashboard"} />
      <Dashboard />
    </>
  );
};

export default DashboardPage;
