import { Box, Typography } from "@aivenio/aquarium";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import Layout from "src/app/layout/Layout";

const NotFound = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <Box role="main" display={"flex"} flexDirection={"column"} gap={"5"}>
          <Typography.Heading color={"secondary-100"}>
            Page not found
          </Typography.Heading>

          <Typography.LargeText>
            If it should have been found, we are working on building it!
          </Typography.LargeText>

          <Typography.MediumText>
            <a href={"/index"}>Go back to old interface</a>
          </Typography.MediumText>
        </Box>
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default NotFound;
