import { Box, Link, Typography } from "@aivenio/aquarium";

const NotFound = () => {
  return (
    <>
      <Box role="main" display={"flex"} flexDirection={"column"} gap={"5"}>
        <Typography.Heading color={"primary-100"}>
          Page not found
        </Typography.Heading>

        <Typography.DefaultStrong>
          Sorry, the page you are looking for does not exist.
        </Typography.DefaultStrong>

        <Typography.Default>
          <Link href={"/index"}>Return to the old interface.</Link>
        </Typography.Default>
      </Box>
    </>
  );
};

export default NotFound;
