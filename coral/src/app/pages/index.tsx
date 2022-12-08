import Layout from "src/app/layout/Layout";
import { PageHeader } from "@aivenio/design-system";

const HomePage = () => {
  return (
    <Layout>
      <PageHeader title={"Homepage"} />
      <h1>Index</h1>
    </Layout>
  );
};

export default HomePage;
