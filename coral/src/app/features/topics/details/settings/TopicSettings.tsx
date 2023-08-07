import {
  Alert,
  BorderBox,
  Box,
  Button,
  PageHeader,
  Skeleton,
  Typography,
  useToast,
} from "@aivenio/aquarium";
import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { requestTopicDeletion, DeleteTopicPayload } from "src/domain/topic";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { parseErrorMsg } from "src/services/mutation-utils";
import { TopicDeleteConfirmationModal } from "src/app/features/topics/details/settings/components/TopicDeleteConfirmationModal";

function TopicSettings() {
  const { topicName, environmentId, topicOverview, topicOverviewIsRefetching } =
    useTopicDetails();

  const isTopicOwner = topicOverview.topicInfo.topicOwner;
  const showDeleteTopic = topicOverview.topicInfo.showDeleteTopic;

  const navigate = useNavigate();
  const toast = useToast();

  const [showConfirmation, setShowConfirmation] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | undefined>();

  const { mutate, isLoading } = useMutation(
    (params: Omit<DeleteTopicPayload, "topicName" | "env">) =>
      requestTopicDeletion({
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
    const topicHasActiveSubscriptions = topicOverview.topicInfo.hasACL;
    const topicIsOnHigherEnvironment = !topicOverview.topicInfo.highestEnv;
    const topicHasPendingRequests = topicOverview.topicInfo.hasOpenRequest;

    return (
      <ul style={{ listStyle: "initial" }}>
        {topicHasActiveSubscriptions && (
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
          {topicOverviewIsRefetching && (
            <div className={"visually-hidden"}>Loading information</div>
          )}
          {topicOverviewIsRefetching && (
            <BorderBox
              date-testid={"topic-settings-danger-zone-content"}
              display={"flex"}
              flexDirection={"column"}
              borderColor={"error-60"}
              padding={"l2"}
              marginTop={"l2"}
              rowGap={"l2"}
            >
              <Box
                display={"flex"}
                alignItems={"center"}
                justifyContent={"space-between"}
              >
                <Box width={"full"}>
                  <Skeleton />
                  <Skeleton />
                </Box>
                <div>
                  {/* eslint-disable-next-line @typescript-eslint/no-empty-function */}
                  <Button.Primary onClick={() => {}} disabled={true}>
                    Delete topic
                  </Button.Primary>
                </div>
              </Box>
            </BorderBox>
          )}

          {!topicOverviewIsRefetching && (
            <BorderBox
              date-testid={"topic-settings-danger-zone-content"}
              display={"flex"}
              flexDirection={"column"}
              borderColor={"error-60"}
              padding={"l2"}
              marginTop={"l2"}
              rowGap={"l2"}
              aria-hidden={topicOverviewIsRefetching}
            >
              {!showDeleteTopic && (
                <Alert type={"warning"}>
                  {topicOverviewIsRefetching ? (
                    <Typography.DefaultStrong htmlTag={"div"}>
                      <Skeleton />
                    </Typography.DefaultStrong>
                  ) : (
                    <>
                      You can not create a delete request for this topic: <br />
                      {getDeleteDisabledInformation()}
                    </>
                  )}
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
          )}
        </>
      )}
    </>
  );
}

export { TopicSettings };
