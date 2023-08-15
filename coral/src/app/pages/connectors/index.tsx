import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseConnectors from "src/app/features/connectors/browse/BrowseConnectors";

const ConnectorsPage = () => {
  const navigate = useNavigate();

  return (
    <>
      <PreviewBanner linkTarget={"/kafkaConnectors"} />
      <PageHeader
        title={"Connectors"}
        primaryAction={{
          text: "Request new connector",
          onClick: () => navigate("/connectors/request"),
          icon: add,
        }}
      />
      <BrowseConnectors />
    </>
  );
};

export default ConnectorsPage;
