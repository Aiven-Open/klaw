import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/dist/src/icons/add";
import { useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import BrowseConnectors from "src/app/features/connectors/browse/BrowseConnectors";
import { useAuthContext } from "src/app/context-provider/AuthProvider";

const ConnectorsPage = () => {
  const navigate = useNavigate();
  const { isSuperAdminUser } = useAuthContext();

  return (
    <>
      <PreviewBanner linkTarget={"/kafkaConnectors"} />
      <PageHeader
        title={"Connectors"}
        primaryAction={
          !isSuperAdminUser
            ? {
                text: "Request new connector",
                onClick: () => navigate("/connectors/request"),
                icon: add,
              }
            : undefined
        }
      />
      <BrowseConnectors />
    </>
  );
};

export default ConnectorsPage;
