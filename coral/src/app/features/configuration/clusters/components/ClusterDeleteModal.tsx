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
        text: "Remove",
        loading: isLoading,
        disabled: !canDeleteCluster,
      }}
      secondaryAction={{
        onClick: handleClose,
        text: "Cancel",
      }}
      title="Remove cluster"
      type="danger"
    >
      <Box.Flex gap="5" flexDirection="column">
        {error !== null && <Alert type="error">{parseErrorMsg(error)}</Alert>}
        {canDeleteCluster ? (
          <Typography.Default>
            Confirm removal of <Chip text={clusterName} />. The cluster will be
            excluded from Klaw.
          </Typography.Default>
        ) : (
          <Typography.Default>
            <Chip text={clusterName} /> is linked to an environment and cannot
            be removed from Klaw. Unlink the environment to proceed.
          </Typography.Default>
        )}
      </Box.Flex>
    </Dialog>
  );
};

export default ClusterDeleteModal;
