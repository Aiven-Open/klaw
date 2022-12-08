import BrowseTopics from "src/app/features/browse-topics/BrowseTopics";
import { PageHeader } from "@aivenio/design-system";
import Layout from "src/app/layout/Layout";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";

const Topics = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <PageHeader title={"Browse all topics"} />
        <BrowseTopics />
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default Topics;
