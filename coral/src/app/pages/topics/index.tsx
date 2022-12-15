import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseTopics from "src/app/features/browse-topics/BrowseTopics";
import Layout from "src/app/layout/Layout";

const Topics = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <PreviewBanner linkTarget={"/browseTopics"} />
        <PageHeader
          title={"All topics"}
          primaryAction={{
            text: "Request new topic",
            // @TODO Replace by useNavigate once the request page is implemented in coral
            onClick: () => (window.location.href = "/requestTopics"),
            icon: add,
          }}
        />
        <BrowseTopics />
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default Topics;
