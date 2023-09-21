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
  TextareaBase,
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
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";
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
    topicSchemasIsRefetching,
    setSchemaVersion,
    topicOverview,
  } = useTopicDetails();
  const [showSchemaPromotionModal, setShowSchemaPromotionModal] =
    useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const isTopicOwner = topicOverview.topicInfo.topicOwner;
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

        const { targetEnvId, sourceEnv } = schemaPromotionDetails;

        if (targetEnvId === undefined || sourceEnv === undefined) {
          throw new Error("No promotion details available");
        }

        return promoteSchemaRequest({
          targetEnvironment: targetEnvId,
          sourceEnvironment: sourceEnv,
          topicName,
          schemaVersion: String(schemaDetailsPerEnv.version),
          forceRegister,
          remarks,
        });
      },
      {
        onError: (error: HTTPError) => {
          setErrorMessage(parseErrorMsg(error));
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
            disabled: topicSchemasIsRefetching,
          }}
        />
      </>
    );
  }

  const { targetEnv, status: promotionStatus } = schemaPromotionDetails;

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
          // And the failure is because of a schema compatibility issue
          showForceRegister={
            errorMessage.length > 0 &&
            errorMessage.includes("Schema is not compatible")
          }
        />
      )}
      <PageHeader title="Schema" />
      <Box.Flex display={"flex"} justifyContent={"space-between"}>
        <Box.Flex display={"flex"} colGap={"l1"}>
          {topicSchemasIsRefetching ? (
            <>
              <div className={"visually-hidden"}>Versions loading</div>
              <NativeSelect.Skeleton />
            </>
          ) : (
            <>
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
                <Box.Flex display={"flex"} marginTop={"3"} colGap={"2"}>
                  <Icon icon={gitNewBranch} style={{ marginTop: "2px" }} />{" "}
                  <span>{allSchemaVersions.length} versions</span>
                </Box.Flex>
              </Typography.SmallStrong>
            </>
          )}
        </Box.Flex>

        {!topicSchemasIsRefetching && isTopicOwner && (
          <Box.Flex alignSelf={"top"}>
            <Link
              to={`/topic/${topicName}/request-schema?env=${schemaDetailsPerEnv.env}`}
            >
              <Button.Primary icon={add} disabled={topicSchemasIsRefetching}>
                Request a new version
              </Button.Primary>
            </Link>
          </Box.Flex>
        )}
      </Box.Flex>

      {!topicSchemasIsRefetching &&
        isTopicOwner &&
        promotionStatus !== "NO_PROMOTION" && (
          <Banner image={illustration} layout="vertical" title={""}>
            <Box.Flex component={"p"} marginBottom={"l1"}>
              This schema has not yet been promoted to the {targetEnv}{" "}
              environment.
            </Box.Flex>
            {errorMessage.length > 0 && (
              <Box.Flex component={"p"} marginBottom={"l1"}>
                <Alert type="error">{errorMessage}</Alert>
              </Box.Flex>
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
        isLoading={topicSchemasIsRefetching}
        version={schemaDetailsPerEnv.version}
        id={schemaDetailsPerEnv.id}
        compatibility={schemaDetailsPerEnv.compatibility.toUpperCase()}
      />
      <Box.Flex marginTop={"l3"} marginBottom={"l2"}>
        <Label>Schema</Label>
        {topicSchemasIsRefetching ? (
          <>
            <div className={"visually-hidden"}>Loading schema preview</div>
            <TextareaBase.Skeleton />
          </>
        ) : (
          <Box.Flex borderColor={"grey-20"} borderWidth={"1px"}>
            <MonacoEditor
              data-testid="topic-schema"
              height="250px"
              language="json"
              theme={"light"}
              value={schemaDetailsPerEnv.content}
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
          </Box.Flex>
        )}
      </Box.Flex>
    </>
  );
}

export { TopicDetailsSchema };
