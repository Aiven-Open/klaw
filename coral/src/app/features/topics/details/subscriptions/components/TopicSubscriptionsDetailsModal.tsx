import { Modal } from "src/app/components/Modal";
import {
  ConsumerOffsets,
  ServiceAccountDetails,
} from "src/domain/acl/acl-types";
import { AclOverviewInfo } from "src/domain/topic/topic-types";

interface TopicSubscriptionsDetailsModalProps {
  closeDetailsModal: () => void;
  isAivenCluster: boolean;
  selectedSub: AclOverviewInfo;
  offsetsData?: ConsumerOffsets;
  serviceAccountData?: ServiceAccountDetails;
}

const TopicSubscriptionsDetailsModal = ({
  closeDetailsModal,
  isAivenCluster,
  selectedSub,
  offsetsData,
  serviceAccountData,
}: TopicSubscriptionsDetailsModalProps) => {
  return (
    <Modal
      title={"Subscription details"}
      primaryAction={{
        text: "Close",
        onClick: closeDetailsModal,
      }}
      close={closeDetailsModal}
    >
      <>
        <p>isAivenClusetr: {String(isAivenCluster)}</p>
        <p>Env: {selectedSub.environmentName}</p>
        <p>ACL type: {selectedSub.topictype}</p>
        <p>ACL pattern: {selectedSub.aclPatternType}</p>
        <p>Topic name: {selectedSub.topicname}</p>
        <p>Consumer group: {selectedSub.consumergroup}</p>
        <p>
          {isAivenCluster ? "Service accounts" : "Principals"}:{" "}
          {selectedSub.acl_ssl}
        </p>
        <p>
          {selectedSub.acl_ip === undefined
            ? null
            : `IP: ${selectedSub.acl_ip}`}
        </p>
        <p>
          {isAivenCluster
            ? `Service account password: ${
                serviceAccountData?.password || "Loading"
              }`
            : null}
        </p>
        <p>
          {!isAivenCluster && selectedSub.topictype === "Consumer"
            ? `Consumer offset: Partition ${
                offsetsData?.topicPartitionId || "Loading"
              } |
              Current offset ${
                offsetsData?.currentOffset || "Loading"
              } | End offset
              ${offsetsData?.endOffset || "Loading"} | Lag ${
                offsetsData?.lag || "Loading"
              }`
            : null}
        </p>
      </>
    </Modal>
  );
};

export default TopicSubscriptionsDetailsModal;
