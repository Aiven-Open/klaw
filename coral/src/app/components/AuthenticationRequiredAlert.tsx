import { Box, Typography } from "@aivenio/aquarium";

function AuthenticationRequiredAlert() {
  return (
    <Box
      role="alertdialog"
      aria-labelledby={"authentication-required-heading"}
      aria-describedby={"authentication-required-text"}
      display={"flex"}
      flexDirection={"column"}
      paddingTop={"l6"}
      justifyContent={"center"}
      alignItems={"center"}
    >
      <Typography.Heading color={"secondary-100"}>
        <span id={"authentication-required-heading"}>
          Authentication session expired
        </span>
      </Typography.Heading>

      <Typography.LargeText>
        <span id={"authentication-required-text"}>
          Redirecting to login page.
        </span>
      </Typography.LargeText>
    </Box>
  );
}

export { AuthenticationRequiredAlert };
