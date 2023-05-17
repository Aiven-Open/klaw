import { Box, Typography } from "@aivenio/aquarium";

const NotFound = () => {
  return (
    <>
      <Box role="main" display={"flex"} flexDirection={"column"} gap={"5"}>
        <Typography.Heading color={"secondary-100"}>
          Page not found
        </Typography.Heading>

        <Typography.LargeText>
          Sorry, the page you are looking for does not exist.
        </Typography.LargeText>

        <Typography.MediumText>
          <a href={"/index"}>Return to the old interface.</a>
        </Typography.MediumText>
      </Box>
    </>
  );
};

export default NotFound;
