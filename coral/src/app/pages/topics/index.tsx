import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useNavigate } from "react-router-dom";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseTopics from "src/app/features/topics/browse/BrowseTopics";

const Topics = () => {
  const navigate = useNavigate();
  return (
    <AuthenticationRequiredBoundary>
      <>
        <PreviewBanner linkTarget={"/browseTopics"} />
        <PageHeader
          title={"All topics"}
          primaryAction={{
            text: "Request new topic",
            onClick: () => navigate("/topics/request"),
            icon: add,
          }}
        />
        <BrowseTopics />
      </>
    </AuthenticationRequiredBoundary>
  );
};

export default Topics;
