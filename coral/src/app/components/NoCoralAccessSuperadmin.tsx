import { Box, Button, Typography } from "@aivenio/aquarium";

function NoCoralAccessSuperadmin() {
  return (
    <Box
      role="dialog"
      aria-labelledby={"superadmin-coral-access"}
      display={"flex"}
      flexDirection={"column"}
      paddingTop={"l6"}
      justifyContent={"center"}
      alignItems={"center"}
      rowGap={"l1"}
    >
      <Typography.Heading color={"primary-100"}>
        <span id={"superadmin-coral-access"}>
          You&apos;re currently logged in as superadmin.
        </span>
      </Typography.Heading>

      <Typography.LargeStrong htmlTag={"p"}>
        To experience the new user interface, switch to your user account.
      </Typography.LargeStrong>
      <Typography.LargeStrong htmlTag={"p"}>
        To continue as superadmin, go to the old interface.
      </Typography.LargeStrong>
      <Box.Flex colGap={"l2"}>
        <Button.ExternalLink href={"/login"}>Login as user</Button.ExternalLink>
        <Button.ExternalLink href={"/"} kind="secondary">
          Go to old interface
        </Button.ExternalLink>
      </Box.Flex>
    </Box>
  );
}

export { NoCoralAccessSuperadmin };
