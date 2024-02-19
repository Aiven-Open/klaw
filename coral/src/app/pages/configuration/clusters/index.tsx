import { PageHeader } from "@aivenio/aquarium";
import add from "@aivenio/aquarium/icons/add";
import { useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import Clusters from "src/app/features/configuration/clusters/Clusters";
import { Routes } from "src/services/router-utils/types";

const ClustersPage = () => {
  const navigate = useNavigate();
  const { permissions } = useAuthContext();

  return (
    <>
      <PreviewBanner linkTarget={"/clusters"} />
      <PageHeader
        title={"Clusters"}
        primaryAction={
          permissions.addDeleteEditClusters
            ? {
                text: "Add new cluster",
                onClick: () => navigate(Routes.ADD_CLUSTER),
                icon: add,
              }
            : undefined
        }
      />
      <Clusters />
    </>
  );
};

export { ClustersPage };
