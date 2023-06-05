import {
  BorderBox,
  Box,
  Button,
  PageHeader,
  Typography,
} from "@aivenio/aquarium";

function TopicSettings() {
  function onDelete() {
    console.log("tbc");
  }

  return (
    <>
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
          <Button.Primary onClick={onDelete}>Delete topic</Button.Primary>
        </div>
      </BorderBox>
    </>
  );
}

export { TopicSettings };
