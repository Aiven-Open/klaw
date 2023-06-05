import {
  BorderBox,
  Box,
  Button,
  PageHeader,
  Typography,
} from "@aivenio/aquarium";
import { Dialog } from "src/app/components/Dialog";
import { useState } from "react";

function TopicSettings() {
  const [showConfirmation, setShowConfirmation] = useState<boolean>(false);
  function deleteTopic() {
    console.log("tbc");
  }

  return (
    <>
      {showConfirmation && (
        <Dialog
          title={"Delete topic"}
          primaryAction={{
            text: "Delete topic",
            onClick: deleteTopic,
            loading: false,
          }}
          secondaryAction={{
            text: "Cancel",
            onClick: () => setShowConfirmation(false),
            disabled: false,
          }}
          type={"danger"}
        >
          Are you sure you want to delete this topic?
        </Dialog>
      )}
      <PageHeader title={"Settings"} />
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
            Once you delete a topic, there is no going back. Please be certain.
          </Box>
        </div>
        <div>
          <Button.Primary onClick={() => setShowConfirmation(true)}>
            Delete topic
          </Button.Primary>
        </div>
      </BorderBox>
    </>
  );
}

export { TopicSettings };
