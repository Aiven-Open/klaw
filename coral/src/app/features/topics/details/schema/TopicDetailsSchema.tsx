import {
  Box,
  EmptyState,
  Icon,
  Label,
  NativeSelect,
  Option,
  PageHeader,
  TextareaBase,
  Typography,
  useToast,
  InlineIcon,
} from "@aivenio/aquarium";
import add from "@aivenio/aquarium/icons/add";
import gitNewBranch from "@aivenio/aquarium/icons/gitNewBranch";
import MonacoEditor from "@monaco-editor/react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { SchemaPromotionModal } from "src/app/features/topics/details/schema/components/SchemaPromotionModal";
import { SchemaStats } from "src/app/features/topics/details/schema/components/SchemaStats";
import {
  PromoteSchemaPayload,
  requestSchemaPromotion,
} from "src/domain/schema-request";
import { HTTPError } from "src/services/api";
import { parseErrorMsg } from "src/services/mutation-utils";
import { SchemaPromotionBanner } from "src/app/features/topics/details/schema/components/SchemaPromotionBanner";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";
import { SchemaPromotableOnlyAlert } from "src/app/features/topics/details/schema/components/SchemaPromotableOnlyAlert";

//@ TODO change to api response value
// eslint-disable-next-line react/prop-types
function TopicDetailsSchema({ createSchemaAllowed = true }) {
  const queryClient = useQueryClient();
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

  const toast = useToast();

  const { topicOwner, hasOpenSchemaRequest } = topicOverview.topicInfo;
  const isTopicOwner = topicOwner;
  const noSchema =
    allSchemaVersions.length === 0 ||
    schemaDetailsPerEnv === undefined ||
    schemaPromotionDetails === undefined;

  const { mutate: promoteSchema, isLoading: promoteSchemaIsLoading } =
    useMutation(
      ({
        forceRegister = false,
        remarks = "",
      }: Pick<PromoteSchemaPayload, "forceRegister" | "remarks">) => {
        if (noSchema) {
          throw new Error("No schema available");
        }

        if (
          schemaPromotionDetails.targetEnvId === undefined ||
          schemaPromotionDetails.sourceEnv === undefined
        ) {
          throw new Error("No promotion details available");
        }

        return requestSchemaPromotion({
          targetEnvironment: schemaPromotionDetails.targetEnvId,
          sourceEnvironment: schemaPromotionDetails.sourceEnv,
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
          queryClient.refetchQueries(["schema-overview"]).then(() => {
            setShowSchemaPromotionModal(false);
            toast({
              message: "Schema promotion request successfully sent",
              position: "bottom-left",
              variant: "default",
            });
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
            disabled: topicSchemasIsRefetching || !createSchemaAllowed,
          }}
        >
          {!createSchemaAllowed && <SchemaPromotableOnlyAlert />}
        </EmptyState>
      </>
    );
  }

  return (
    <>
      {showSchemaPromotionModal &&
        schemaPromotionDetails.targetEnv !== undefined && (
          <SchemaPromotionModal
            isLoading={promoteSchemaIsLoading}
            onSubmit={promoteSchema}
            onClose={() => setShowSchemaPromotionModal(false)}
            targetEnvironment={schemaPromotionDetails.targetEnv}
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
      <Box display={"flex"} justifyContent={"space-between"}>
        <Box display={"flex"} colGap={"l1"}>
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
                <Box display={"flex"} marginTop={"3"} colGap={"2"}>
                  <Icon icon={gitNewBranch} style={{ marginTop: "2px" }} />{" "}
                  <span>{allSchemaVersions.length} versions</span>
                </Box>
              </Typography.SmallStrong>
            </>
          )}
        </Box>

        {!topicSchemasIsRefetching && isTopicOwner && (
          // In case user can not create a new schema, we don't want the disabled "link" to show up
          // as it makes it harder to convey the information for assistive technology
          <Box alignSelf={"top"} aria-hidden={!createSchemaAllowed}>
            <InternalLinkButton
              to={`/topic/${topicName}/request-schema?env=${schemaDetailsPerEnv.env}`}
              disabled={!createSchemaAllowed}
            >
              <Box.Flex component={"span"} alignItems={"center"} colGap={"3"}>
                <InlineIcon
                  icon={add}
                  scale={2}
                  style={{
                    fontSize: "20px",
                  }}
                />{" "}
                Request a new version
              </Box.Flex>
            </InternalLinkButton>
          </Box>
        )}
      </Box>

      {!createSchemaAllowed && (
        <SchemaPromotableOnlyAlert marginBottom={"l2"} />
      )}
      {!topicSchemasIsRefetching && isTopicOwner && (
        <SchemaPromotionBanner
          schemaPromotionDetails={schemaPromotionDetails}
          hasOpenSchemaRequest={hasOpenSchemaRequest}
          topicName={topicName}
          setShowSchemaPromotionModal={() =>
            setShowSchemaPromotionModal(!showSchemaPromotionModal)
          }
          hasError={errorMessage.length > 0}
          errorMessage={errorMessage}
        />
      )}

      <SchemaStats
        isLoading={topicSchemasIsRefetching}
        version={schemaDetailsPerEnv.version}
        id={schemaDetailsPerEnv.id}
        compatibility={schemaDetailsPerEnv.compatibility.toUpperCase()}
      />
      <Box marginTop={"l3"} marginBottom={"l2"}>
        <Label>Schema</Label>
        {topicSchemasIsRefetching ? (
          <>
            <div className={"visually-hidden"}>Loading schema preview</div>
            <TextareaBase.Skeleton />
          </>
        ) : (
          <Box borderColor={"grey-20"} borderWidth={"1px"}>
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
          </Box>
        )}
      </Box>
    </>
  );
}

export { TopicDetailsSchema };
