import { useQuery } from "@tanstack/react-query";
import { Modal } from "src/app/components/Modal";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import {
  getAivenServiceAccountDetails,
  getConsumerOffsets,
} from "src/domain/acl/acl-api";
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
}: TopicSubscriptionsDetailsModalProps) => {
  const user = useAuthContext();
  const { environment, consumergroup, topicname, req_no } = selectedSub;

  const {
    data: offsetsData,
    // @TODO: handle loading / error state gracefully
    // isError: offsetsIsError,
    // error: offsetsError,
    // isFetched: offsetDataFetched,
  } = useQuery(["consumer-offsets", environment, consumergroup], {
    queryFn: () => {
      if (environment !== undefined && consumergroup !== undefined) {
        return getConsumerOffsets({
          topicName: topicname,
          env: environment,
          consumerGroupId: consumergroup,
        });
      }
    },
    enabled:
      selectedSub.environment !== undefined &&
      selectedSub.consumergroup !== undefined,
  });

  const {
    data: serviceAccountData,
    // @TODO: handle loading / error state gracefully
    // isError: serviceAccountIsError,
    // error: serviceAccountError,
    // isFetched: serviceAccountDataFetched,
  } = useQuery(
    [
      "getAivenServiceAccountDetails",
      environment,
      topicname,
      user?.username,
      req_no,
    ],
    {
      queryFn: () => {
        if (
          environment !== undefined &&
          req_no !== undefined &&
          user?.username !== undefined
        ) {
          return getAivenServiceAccountDetails({
            env: environment,
            topicName: topicname,
            userName: user.username,
            aclReqNo: req_no,
          });
        }
      },
      enabled: environment !== undefined && req_no !== undefined,
    }
  );

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
          {!isAivenCluster &&
          selectedSub.topictype === "Consumer" &&
          offsetsData !== undefined &&
          offsetsData[0] !== undefined
            ? `Consumer offset: Partition ${
                offsetsData[0]?.topicPartitionId || "Loading"
              } |
              Current offset ${
                offsetsData[0]?.currentOffset || "Loading"
              } | End offset
              ${offsetsData[0]?.endOffset || "Loading"} | Lag ${
                offsetsData[0]?.lag || "Loading"
              }`
            : null}
        </p>
      </>
    </Modal>
  );
};

export default TopicSubscriptionsDetailsModal;
