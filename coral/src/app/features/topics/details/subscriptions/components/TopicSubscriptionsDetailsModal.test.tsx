import { cleanup } from "@testing-library/react";
import TopicSubscriptionsDetailsModal from "src/app/features/topics/details/subscriptions/components/TopicSubscriptionsDetailsModal";
import { AclOverviewInfo } from "src/domain/topic/topic-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockCloseDetailsModal = jest.fn();

const testServiceAccountData = {
  username: "nkira",
  password: "service-account-pw",
  accountFound: true,
};

const testOffsetsData = {
  topicPartitionId: "0",
  currentOffset: "0",
  endOffset: "0",
  lag: "0",
};

const testSelectedSub: AclOverviewInfo = {
  req_no: "1006",
  acl_ssl: "aivtopic3user",
  topicname: "aivtopic3",
  topictype: "Producer",
  consumergroup: "-na-",
  environment: "1",
  environmentName: "DEV",
  teamname: "Ospo",
  teamid: 1003,
  aclPatternType: "LITERAL",
  showDeleteAcl: true,
  kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
};

const defaultProps = {
  closeDetailsModal: mockCloseDetailsModal,
  isAivenCluster: true,
  selectedSub: testSelectedSub,
  offsetsData: testOffsetsData,
  serviceAccountData: testServiceAccountData,
};

describe("TopicSubscriptionsDetailsModal.tsx", () => {
  afterAll(() => {
    cleanup();
  });

  // @TODO finish testing in PR handling proper display of the data
  it("should render correct data in details modal", async () => {
    customRender(<TopicSubscriptionsDetailsModal {...defaultProps} />, {
      queryClient: true,
    });
  });
});
