import { Typography } from "@aivenio/aquarium";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";
import Layout from "src/app/layout/Layout";

const NotFound = () => {
  return (
    <AuthenticationRequiredBoundary>
      <Layout>
        <Typography.Heading color={"secondary-100"}>
          Page not found
        </Typography.Heading>

        <Typography.LargeText>
          If it should have been found, we are working on building it!
        </Typography.LargeText>
      </Layout>
    </AuthenticationRequiredBoundary>
  );
};

export default NotFound;
