import {
  Box,
  Button,
  EmptyState,
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
import { Link, useNavigate } from "react-router-dom";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { SchemaPromotionBanner } from "src/app/features/topics/details/schema/components/SchemaPromotionBanner";
import { SchemaStats } from "src/app/features/topics/details/schema/components/SchemaStats";

function TopicDetailsSchema() {
  const navigate = useNavigate();
  const {
    topicName,
    topicSchemas: {
      allSchemaVersions = [],
      latestVersion,
      schemaDetailsPerEnv,
    },
    setSchemaVersion,
  } = useTopicDetails();

  const noSchema = allSchemaVersions.length === 0;

  function promoteSchema() {
    console.log("dummy function");
  }

  if (noSchema) {
    return (
      <>
        <PageHeader title="Schema" />
        <EmptyState
          title="No schema available for this topic"
          primaryAction={{
            onClick: () => navigate(`/topic/${topicName}/request-schema`),
            text: "Request a new schema",
          }}
        />
      </>
    );
  }

  return (
    <>
      <PageHeader title="Schema" />

      <Box display={"flex"} justifyContent={"space-between"}>
        <Box display={"flex"} colGap={"l1"}>
          <NativeSelect
            style={{ width: "300px" }}
            aria-label={"Select version"}
            onChange={(e) => setSchemaVersion(Number(e.target.value))}
            defaultValue={schemaDetailsPerEnv?.version}
          >
            {allSchemaVersions.map((version) => (
              <Option key={version} value={version}>
                Version {version} {version === latestVersion && "(latest)"}
              </Option>
            ))}
          </NativeSelect>
          <Typography.SmallStrong color={"grey-40"}>
            <Box display={"flex"} marginTop={"3"} colGap={"2"}>
              <Icon icon={gitNewBranch} style={{ marginTop: "2px" }} />{" "}
              <span>{allSchemaVersions.length} versions</span>
            </Box>
          </Typography.SmallStrong>
        </Box>

        <Box alignSelf={"top"}>
          <Link
            to={`/topic/${topicName}/request-schema?env=${schemaDetailsPerEnv?.env}`}
          >
            <Button.Primary icon={add}>Request a new version</Button.Primary>
          </Link>
        </Box>
      </Box>

      {/*@TODO pass data when API verified */}
      <SchemaPromotionBanner
        environment={"TST"}
        promoteSchema={promoteSchema}
      />

      <SchemaStats
        version={schemaDetailsPerEnv?.version || 0}
        id={schemaDetailsPerEnv?.id || 0}
        compatibility={
          schemaDetailsPerEnv?.compatibility || "Couldn't retrieve"
        }
      />

      <Box marginTop={"l3"} marginBottom={"l2"}>
        <Label>Schema</Label>

        <Box borderColor={"grey-20"} borderWidth={"1px"}>
          <MonacoEditor
            data-testid="topic-schema"
            height="250px"
            language="json"
            theme={"light"}
            value={schemaDetailsPerEnv?.content}
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
