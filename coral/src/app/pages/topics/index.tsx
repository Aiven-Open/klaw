import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseTopics from "src/app/features/topics/browse/BrowseTopics";
import { useAuthContext } from "src/app/context-provider/AuthProvider";

const Topics = () => {
  const navigate = useNavigate();
  const { isSuperAdminUser } = useAuthContext();

  return (
    <>
      <PreviewBanner linkTarget={"/browseTopics"} />
      <PageHeader
        title={"Topics"}
        primaryAction={
          !isSuperAdminUser
            ? {
                text: "Request new topic",
                onClick: () => navigate("/topics/request"),
                icon: add,
              }
            : undefined
        }
      />
      <BrowseTopics />
    </>
  );
};

export default Topics;
