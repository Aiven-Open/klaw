import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseTopics from "src/app/features/topics/browse/BrowseTopics";
import { KlawProvider } from "klaw";
import { getTopics } from "src/domain/topic";
import { KlawApiRequestQueryParameters } from "types/utils";

const sources = {
  getTopics: (params: unknown) =>
    getTopics(params as KlawApiRequestQueryParameters<"getTopics">).then(
      (res) => ({
        topics: res.entries.map((topic) => ({
          topicName: topic.topicName,
        })),
      })
    ),
};

const Topics = () => {
  const navigate = useNavigate();
  return (
    <KlawProvider sources={sources}>
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
    </KlawProvider>
  );
};

export default Topics;
