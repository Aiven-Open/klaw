import {
  Alert,
  BorderBox,
  Box,
  Button,
  PageHeader,
  Typography,
  useToast,
} from "@aivenio/aquarium";
import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { deleteTopic, DeleteTopicPayload } from "src/domain/topic";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { parseErrorMsg } from "src/services/mutation-utils";
import { TopicDeleteConfirmationModal } from "src/app/features/topics/details/settings/components/TopicDeleteConfirmationModal";

function TopicSettings() {
  const { topicName, environmentId, topicOverview } = useTopicDetails();

  const isTopicOwner = topicOverview.topicInfo.topicOwner;
  const showDeleteTopic = topicOverview.topicInfo.showDeleteTopic;

  const navigate = useNavigate();
  const toast = useToast();

  const [showConfirmation, setShowConfirmation] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | undefined>();

  const { mutate, isLoading } = useMutation(
    (params: Omit<DeleteTopicPayload, "topicName" | "env">) =>
      deleteTopic({
        topicName,
        env: environmentId,
        deleteAssociatedSchema: params.deleteAssociatedSchema,
        remark: params.remark,
      }),
    {
      onSuccess: () => {
        navigate("/topics");
        toast({
          message: "Topic deletion request successfully created",
          position: "bottom-left",
          variant: "default",
        });
      },
      onError: (error) => {
        setErrorMessage(parseErrorMsg(error));
      },
      onSettled: () => {
        setShowConfirmation(false);
      },
    }
  );

  function getDeleteDisabledInformation() {
    const topicHasOpenACLRequest = topicOverview.topicInfo.hasOpenACLRequest;
    const topicIsOnHigherEnvironment = !topicOverview.topicInfo.highestEnv;
    const topicHasPendingRequests = topicOverview.topicInfo.hasOpenRequest;

    return (
      <ul style={{ listStyle: "initial" }}>
        {topicHasOpenACLRequest && (
          <li>
            The topic has active subscriptions. Please delete them before
            deleting the topic.
          </li>
        )}
        {topicIsOnHigherEnvironment && (
          <li>
            The topic is on a higher environment. Please delete the topic from
            that environment first.
          </li>
        )}
        {topicHasPendingRequests && <li>The topic has a pending request.</li>}
      </ul>
    );
  }

  return (
    <>
      {showConfirmation && (
        <TopicDeleteConfirmationModal
          isLoading={isLoading}
          onSubmit={({
            remark,
            deleteAssociatedSchema,
          }: {
            remark?: string;
            deleteAssociatedSchema: boolean;
          }) => mutate({ remark, deleteAssociatedSchema })}
          onClose={() => setShowConfirmation(false)}
        />
      )}

      <PageHeader title={"Settings"} />
      {errorMessage && (
        <Box role="alert" marginBottom={"l2"}>
          <Alert type="error">{errorMessage}</Alert>
        </Box>
      )}

      {!isTopicOwner && (
        <div>
          Settings can only be edited by team members of the team the topic does
          belong to.
        </div>
      )}

      {isTopicOwner && (
        <>
          <Typography.Subheading>Danger zone</Typography.Subheading>
          <BorderBox
            display={"flex"}
            flexDirection={"column"}
            borderColor={"error-60"}
            padding={"l2"}
            marginTop={"l2"}
            rowGap={"l2"}
          >
            {!showDeleteTopic && (
              <Alert type={"warning"}>
                <>
                  You can not create a delete request for this topic: <br />
                  {getDeleteDisabledInformation()}
                </>
              </Alert>
            )}
            <Box
              display={"flex"}
              alignItems={"center"}
              justifyContent={"space-between"}
            >
              <div>
                <Typography.DefaultStrong htmlTag={"h3"}>
                  Delete this topic
                </Typography.DefaultStrong>
                <Box component={"p"}>
                  Once you delete a topic, there is no going back. Please be
                  certain.
                </Box>
              </div>

              <div>
                <Button.Primary
                  onClick={() => setShowConfirmation(true)}
                  disabled={!showDeleteTopic}
                >
                  Delete topic
                </Button.Primary>
              </div>
            </Box>
          </BorderBox>
        </>
      )}
    </>
  );
}

export { TopicSettings };
