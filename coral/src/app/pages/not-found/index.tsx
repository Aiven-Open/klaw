import { Box, Link, Typography } from "@aivenio/aquarium";

const NotFound = () => {
  return (
    <>
      <Box role="main" display={"flex"} flexDirection={"column"} gap={"5"}>
        <Typography.Heading color={"primary-100"}>
          Page not found
        </Typography.Heading>

        <Typography.LargeStrong>
          Sorry, the page you are looking for does not exist.
        </Typography.LargeStrong>

        <Typography.DefaultStrong>
          <Link href={"/index"}>Return to the old interface.</Link>
        </Typography.DefaultStrong>
      </Box>
    </>
  );
};

export default NotFound;
