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
import { deleteTopic } from "src/domain/topic";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { parseErrorMsg } from "src/services/mutation-utils";
import { TopicDeleteConfirmationModal } from "src/app/features/topics/details/settings/components/TopicDeleteConfirmationModal";

function TopicSettings() {
  const { topicName, environmentId, userCanDeleteTopic } = useTopicDetails();
  const navigate = useNavigate();
  const toast = useToast();

  const [showConfirmation, setShowConfirmation] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | undefined>();

  const { mutate, isLoading } = useMutation(
    (params: { deleteAssociatedSchema: boolean; remark: string | undefined }) =>
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
          message: "Topic deletion request successfully sent",
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

      {!userCanDeleteTopic && (
        <div>
          Settings can only be edited by team members of the team the topic does
          belong to.
        </div>
      )}

      {userCanDeleteTopic && (
        <>
          <Typography.Subheading>Danger zone</Typography.Subheading>
          <BorderBox
            display={"flex"}
            borderColor={"warning-100"}
            padding={"l2"}
            alignItems={"center"}
            justifyContent={"space-between"}
            marginTop={"l2"}
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
              <Button.Primary onClick={() => setShowConfirmation(true)}>
                Delete topic
              </Button.Primary>
            </div>
          </BorderBox>
        </>
      )}
    </>
  );
}

export { TopicSettings };
