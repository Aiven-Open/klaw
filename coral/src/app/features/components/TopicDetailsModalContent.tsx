import { BorderBox, Box, Grid, GridItem, StatusChip } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import fromPairs from "lodash/fromPairs";
import isEmpty from "lodash/isEmpty";
import { TopicRequest } from "src/domain/topic/topic-types";

interface DetailsModalContentProps {
  topicRequest?: TopicRequest;
}

const formatAdvancedConfig = (
  entries: TopicRequest["advancedTopicConfigEntries"]
) => {
  if (entries === undefined) {
    return "";
  }

  const entriesToFlatObject = fromPairs(
    entries.map((config) => [config.configKey, config.configValue])
  );

  const flatObjectToJsonString = JSON.stringify(entriesToFlatObject, null, 2);
  return flatObjectToJsonString;
};

const Label = ({ children }: { children: React.ReactNode }) => (
  <dt className="inline-block mb-2 typography-small-strong text-grey-60">
    {children}
  </dt>
);

const TopicDetailsModalContent = ({
  topicRequest,
}: DetailsModalContentProps) => {
  if (topicRequest === undefined) {
    return <div>Request not found.</div>;
  }

  const {
    environmentName,
    requestOperationType,
    topicname,
    description,
    topicpartitions,
    replicationfactor,
    advancedTopicConfigEntries,
    remarks,
    requestor,
    requesttimestring,
  } = topicRequest;

  const hasAdvancedConfig =
    advancedTopicConfigEntries?.length > 0 &&
    !isEmpty(advancedTopicConfigEntries);

  return (
    <Grid htmlTag={"dl"} cols={"2"} rowGap={"6"}>
      <Box.Flex direction={"column"}>
        <Label>Environment</Label>
        <dd>
          <StatusChip status={"neutral"} text={environmentName} />
        </dd>
      </Box.Flex>
      <Box.Flex direction={"column"}>
        <Label>Request type</Label>
        <dd>
          <StatusChip status={"neutral"} text={requestOperationType} />
        </dd>
      </Box.Flex>
      <GridItem colSpan={"span-2"}>
        <Box.Flex direction={"column"}>
          <Label>Topic</Label>
          <dd>{topicname}</dd>
        </Box.Flex>
      </GridItem>
      <GridItem colSpan={"span-2"}>
        <Box.Flex direction={"column"}>
          <Label>Topic description</Label>
          <dd>{description}</dd>
        </Box.Flex>
      </GridItem>
      <Box.Flex direction={"column"}>
        <Label>Topic partition</Label>
        <dd>{topicpartitions}</dd>
      </Box.Flex>
      <Box.Flex direction={"column"}>
        <Label>Topic replication factor</Label>
        <dd>{replicationfactor}</dd>
      </Box.Flex>
      {hasAdvancedConfig && (
        <GridItem colSpan={"span-2"}>
          <Box.Flex direction={"column"}>
            <Label>Advanced configuration</Label>
            <BorderBox borderColor={"grey-20"}>
              <MonacoEditor
                data-testid="topic-advanced-config"
                language="json"
                height={"100px"}
                theme={"light"}
                value={formatAdvancedConfig(advancedTopicConfigEntries)}
                options={{
                  ariaLabel: "Advanced configuration",
                  readOnly: true,
                  domReadOnly: true,
                  renderControlCharacters: false,
                  minimap: { enabled: false },
                  folding: false,
                  lineNumbers: "off",
                  scrollBeyondLastLine: false,
                }}
              />
            </BorderBox>
          </Box.Flex>
        </GridItem>
      )}
      <GridItem colSpan={"span-2"}>
        <Box.Flex direction={"column"}>
          <Label>Message for approval</Label>
          <dd>{remarks || <i>No message</i>}</dd>
        </Box.Flex>
      </GridItem>
      <Box.Flex direction={"column"}>
        <Label>Requested by</Label>
        <dd>{requestor}</dd>
      </Box.Flex>
      <Box.Flex direction={"column"}>
        <Label>Requested on</Label>
        <dd>{requesttimestring} UTC</dd>
      </Box.Flex>
    </Grid>
  );
};

export default TopicDetailsModalContent;
