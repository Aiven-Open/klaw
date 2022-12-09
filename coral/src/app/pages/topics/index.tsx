import { PageHeader } from "@aivenio/design-system";
import add from "@aivenio/design-system/dist/src/icons/add";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import BrowseTopics from "src/app/features/browse-topics/BrowseTopics";
import Layout from "src/app/layout/Layout";

const Topics = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <PageHeader
          title={"Browse all topics"}
          primaryAction={{
            text: "Request A New Topic",
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
