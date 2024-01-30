import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseTopics from "src/app/features/topics/browse/BrowseTopics";

const Topics = () => {
  const navigate = useNavigate();
  return (
    <>
      <PreviewBanner linkTarget={"/browseTopics"} />
      <PageHeader
        title={"Topics"}
        primaryAction={{
          text: "Request new topic",
          onClick: () => navigate("/topics/request"),
          icon: add,
        }}
      />
      <BrowseTopics />
    </>
  );
};

export default Topics;
