import { PageHeader, Button, Box } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/topics/details/documentation/components/NoDocumentationBanner";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { DocumentationView } from "src/app/features/topics/details/documentation/components/DocumentationView";

function TopicDocumentation() {
  const { topicOverview } = useTopicDetails();

  return (
    <>
      <PageHeader title={"Documentation"} />
      {!topicOverview?.topicDocumentation ||
      topicOverview.topicDocumentation.length === 0 ? (
        <NoDocumentationBanner />
      ) : (
        <Box.Flex flexDirection="column" rowGap={"l2"}>
          <DocumentationView
            stringifiedHtml={topicOverview.topicDocumentation}
          />

          <Box grow={"0"} alignSelf={"end"}>
            <Button.Primary onClick={() => console.log("update")}>
              Update documentation
            </Button.Primary>
          </Box>
        </Box.Flex>
      )}
    </>
  );
}

export { TopicDocumentation };
