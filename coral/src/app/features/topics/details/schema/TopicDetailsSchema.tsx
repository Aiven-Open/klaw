import {
  Alert,
  Banner,
  Box,
  Button,
  EmptyState,
  Icon,
  Label,
  NativeSelect,
  Option,
  PageHeader,
  Typography,
  useToast,
} from "@aivenio/aquarium";
import add from "@aivenio/aquarium/icons/add";
import gitNewBranch from "@aivenio/aquarium/icons/gitNewBranch";
import MonacoEditor from "@monaco-editor/react";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { SchemaPromotionModal } from "src/app/features/topics/details/schema/components/SchemaPromotionModal";
import { SchemaStats } from "src/app/features/topics/details/schema/components/SchemaStats";
import {
  PromoteSchemaPayload,
  promoteSchemaRequest,
} from "src/domain/schema-request";
import illustration from "/src/app/images/topic-details-schema-Illustration.svg";

function TopicDetailsSchema() {
  const navigate = useNavigate();
  const {
    topicName,
    topicSchemas: {
      allSchemaVersions = [],
      latestVersion,
      schemaDetailsPerEnv,
      schemaPromotionDetails,
    },
    setSchemaVersion,
    topicOverview: { topicInfoList },
  } = useTopicDetails();
  const [showSchemaPromotionModal, setShowSchemaPromotionModal] =
    useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const isTopicOwner = topicInfoList[0].topicOwner;
  const noSchema =
    allSchemaVersions.length === 0 ||
    schemaDetailsPerEnv === undefined ||
    schemaPromotionDetails === undefined;

  const toast = useToast();

  const { mutate: promoteSchema, isLoading: promoteSchemaIsLoading } =
    useMutation(
      ({
        forceRegister = false,
        remarks = "",
      }: Pick<PromoteSchemaPayload, "forceRegister" | "remarks">) => {
        if (noSchema) {
          throw new Error("No schema available");
        }

        const { targetEnvId, sourceEnv } =
          schemaPromotionDetails[schemaDetailsPerEnv.env] ?? {};

        if (targetEnvId === undefined || sourceEnv === undefined) {
          throw new Error("No promotion details available available");
        }

        return promoteSchemaRequest({
          targetEnvironment: targetEnvId,
          sourceEnvironment: sourceEnv,
          topicName,
          schemaVersion: String(schemaDetailsPerEnv.version),
          schemaFull: schemaDetailsPerEnv.content,
          forceRegister,
          // @TODO: get the appname form topicOverview, probably needs to be added in the backend?
          appName: "App",
          remarks,
        });
      },
      {
        onError: (error: Error) => {
          setErrorMessage(error.message);
          setShowSchemaPromotionModal(false);
        },
        onSuccess: () => {
          setErrorMessage("");
          setShowSchemaPromotionModal(false);
          toast({
            message: "Schema promotion request successfully sent",
            position: "bottom-left",
            variant: "default",
          });
        },
      }
    );

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

  const { targetEnv, status: promotionStatus } =
    schemaPromotionDetails[schemaDetailsPerEnv.env] ?? {};

  return (
    <>
      {showSchemaPromotionModal && targetEnv !== undefined && (
        <SchemaPromotionModal
          isLoading={promoteSchemaIsLoading}
          onSubmit={promoteSchema}
          onClose={() => setShowSchemaPromotionModal(false)}
          targetEnvironment={targetEnv}
          version={schemaDetailsPerEnv.version}
          // We only allow users to use the forceRegister option when the promotion request failed
          // And the failure is not because of an already existing promotion request
          showForceRegister={
            errorMessage.length > 0 &&
            errorMessage !== "Failure. A request already exists for this topic."
          }
        />
      )}
      <PageHeader title="Schema" />
      <Box display={"flex"} justifyContent={"space-between"}>
        <Box display={"flex"} colGap={"l1"}>
          <NativeSelect
            style={{ width: "300px" }}
            aria-label={"Select version"}
            onChange={(e) => setSchemaVersion(Number(e.target.value))}
            defaultValue={schemaDetailsPerEnv.version}
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

        {isTopicOwner && (
          <Box alignSelf={"top"}>
            <Link
              to={`/topic/${topicName}/request-schema?env=${schemaDetailsPerEnv.env}`}
            >
              <Button.Primary icon={add}>Request a new version</Button.Primary>
            </Link>
          </Box>
        )}
      </Box>
      {isTopicOwner && promotionStatus !== "NO_PROMOTION" && (
        <Banner image={illustration} layout="vertical" title={""}>
          <Box element={"p"} marginBottom={"l1"}>
            This schema has not yet been promoted to the {targetEnv}{" "}
            environment.
          </Box>
          {errorMessage.length > 0 && (
            <Box element={"p"} marginBottom={"l1"}>
              <Alert type="error">{errorMessage}</Alert>
            </Box>
          )}
          <Button.Primary
            onClick={() =>
              setShowSchemaPromotionModal(!showSchemaPromotionModal)
            }
          >
            Promote
          </Button.Primary>
        </Banner>
      )}
      <SchemaStats
        version={schemaDetailsPerEnv.version}
        id={schemaDetailsPerEnv.id}
        compatibility={schemaDetailsPerEnv.compatibility.toUpperCase()}
      />
      <Box marginTop={"l3"} marginBottom={"l2"}>
        <Label>Schema</Label>

        <Box borderColor={"grey-20"} borderWidth={"1px"}>
          <MonacoEditor
            data-testid="topic-schema"
            height="250px"
            language="json"
            theme={"light"}
            value={schemaDetailsPerEnv.content}
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
    </>
  );
}

export { TopicDetailsSchema };
