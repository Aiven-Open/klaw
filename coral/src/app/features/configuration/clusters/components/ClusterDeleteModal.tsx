import { Alert, Box, Chip, Typography } from "@aivenio/aquarium";
import { Dialog } from "src/app/components/Dialog";
import { parseErrorMsg } from "src/services/mutation-utils";

interface ClusterDeleteModalProps {
  handleClose: () => void;
  handleDelete: (clusterId: string) => void;
  clusterId: number;
  clusterName: string;
  canDeleteCluster: boolean;
  isLoading: boolean;
  error: unknown | null;
}

const ClusterDeleteModal = ({
  handleClose,
  handleDelete,
  isLoading,
  clusterId,
  clusterName,
  canDeleteCluster,
  error,
}: ClusterDeleteModalProps) => {
  return (
    <Dialog
      primaryAction={{
        onClick: () => handleDelete(String(clusterId)),
        text: "Delete cluster",
        loading: isLoading,
        disabled: !canDeleteCluster,
      }}
      secondaryAction={{
        onClick: handleClose,
        text: "Cancel",
      }}
      title="Delete cluster"
      type="danger"
    >
      <Box.Flex gap="5" flexDirection="column">
        {error !== null && <Alert type="error">{parseErrorMsg(error)}</Alert>}
        {canDeleteCluster ? (
          <Typography.Default>
            Deleting cluster <Chip text={clusterName} />.
          </Typography.Default>
        ) : (
          <Typography.Default>
            Cannot delete cluster <Chip text={clusterName} />: an environment is
            associated with it.
          </Typography.Default>
        )}
      </Box.Flex>
    </Dialog>
  );
};

export default ClusterDeleteModal;
