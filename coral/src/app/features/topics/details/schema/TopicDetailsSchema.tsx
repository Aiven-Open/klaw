import {
  Box,
  Button,
  Icon,
  Label,
  NativeSelect,
  Option,
  PageHeader,
  Typography,
} from "@aivenio/aquarium";
import add from "@aivenio/aquarium/icons/add";
import gitNewBranch from "@aivenio/aquarium/icons/gitNewBranch";
import MonacoEditor from "@monaco-editor/react";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { SchemaPromotionBanner } from "src/app/features/topics/details/schema/components/SchemaPromotionBanner";
import { SchemaStats } from "src/app/features/topics/details/schema/components/SchemaStats";

function TopicDetailsSchema() {
  const { topicName, environmentId } = useTopicDetails();

  function promoteSchema() {
    console.log("dummy function");
  }

  return (
    <>
      <PageHeader title="Schema" />
      <Box display={"flex"} justifyContent={"space-between"}>
        <Box display={"flex"} colGap={"l1"}>
          <NativeSelect
            style={{ width: "300px" }}
            aria-label={"Select version"}
          >
            <Option key={"1"} value={"1"}>
              version 1
            </Option>
          </NativeSelect>
          <Typography.SmallTextBold color={"grey-40"}>
            <Box display={"flex"} marginTop={"3"} colGap={"2"}>
              <Icon icon={gitNewBranch} style={{ marginTop: "2px" }} />{" "}
              <span>5 versions</span>
            </Box>
          </Typography.SmallTextBold>
        </Box>

        <Box alignSelf={"top"}>
          <Button.ExternalLink
            //@TODO verify if environmentId is the right one
            href={`/topic/${topicName}/request-schema?env=${environmentId}`}
            icon={add}
          >
            Request a new version
          </Button.ExternalLink>
        </Box>
      </Box>

      {/*@TODO pass data when API verified */}
      <SchemaPromotionBanner
        environment={"TST"}
        promoteSchema={promoteSchema}
      />

      <SchemaStats version={99} id={999} compatibility={"BACKWARDS"} />

      <Box marginTop={"l3"} marginBottom={"l2"}>
        <Label>Schema</Label>

        <Box borderColor={"grey-20"} borderWidth={"1px"}>
          <MonacoEditor
            data-testid="topic-schema"
            height="250px"
            language="json"
            theme={"light"}
            value={"{ huhu: 'huhu' }"}
            loading={"Loading preview"}
            options={{
              ariaLabel: "Schema preview",
              readOnly: true,
              domReadOnly: true,
              renderControlCharacters: false,
              minimap: { enabled: false },
              folding: false,
              lineNumbers: "off",
              scrollBeyondLastLine: false,
            }}
          />
        </Box>
      </Box>

      <Button.Secondary>Delete Schema</Button.Secondary>
    </>
  );
}

export { TopicDetailsSchema };
