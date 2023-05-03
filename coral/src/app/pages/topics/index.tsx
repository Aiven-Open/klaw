import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseTopics from "src/app/features/topics/browse/BrowseTopics";
import Layout from "src/app/layout/Layout";

const Topics = () => {
  const navigate = useNavigate();
  return (
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
  );
};

export default Topics;
