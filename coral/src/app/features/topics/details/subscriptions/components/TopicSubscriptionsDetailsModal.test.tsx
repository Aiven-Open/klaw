import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import TopicSubscriptionsDetailsModal from "src/app/features/topics/details/subscriptions/components/TopicSubscriptionsDetailsModal";
import {
  getAivenServiceAccountDetails,
  getConsumerOffsets,
} from "src/domain/acl/acl-api";
import { AclOverviewInfo } from "src/domain/topic/topic-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api");

const mockGetConsumerOffsets = getConsumerOffsets as jest.MockedFunction<
  typeof getConsumerOffsets
>;
const mockGetAivenServiceAccountDetails =
  getAivenServiceAccountDetails as jest.MockedFunction<
    typeof getAivenServiceAccountDetails
  >;

const mockCloseDetailsModal = jest.fn();

const testServiceAccountData = {
  username: "nkira",
  password: "service-account-pw",
  accountFound: true,
};

const notOwnerTestServiceAccountData = {
  accountFound: false,
};

const testOffsetsData = {
  topicPartitionId: "0",
  currentOffset: "0",
  endOffset: "0",
  lag: "0",
};

const testSelectedSubAiven: AclOverviewInfo = {
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

const testSelectedSubNonAiven: AclOverviewInfo = {
  req_no: "1006",
  acl_ip: "0.0.0.0",
  topicname: "aivtopic3",
  topictype: "Consumer",
  consumergroup: "-na-",
  environment: "1",
  environmentName: "DEV",
  teamname: "Ospo",
  teamid: 1003,
  aclPatternType: "IP",
  showDeleteAcl: true,
  kafkaFlavorType: "APACHE_KAFKA",
};

const testNotOwnerSelectedSubAiven: AclOverviewInfo = {
  req_no: "1006",
  acl_ssl: "Not Authorized to see this",
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

const defaultPropsAiven = {
  closeDetailsModal: mockCloseDetailsModal,
  isAivenCluster: true,
  selectedSubscription: testSelectedSubAiven,
  serviceAccountData: testServiceAccountData,
};

const defaultPropsNonAiven = {
  closeDetailsModal: mockCloseDetailsModal,
  isAivenCluster: false,
  selectedSubscription: testSelectedSubNonAiven,
  offsetsData: testOffsetsData,
};

const defaultPropsNotOwnerAiven = {
  closeDetailsModal: mockCloseDetailsModal,
  isAivenCluster: true,
  selectedSubscription: testNotOwnerSelectedSubAiven,
  offsetsData: testOffsetsData,
};

const findDefinition = (term?: string) => {
  return screen
    .getAllByRole("definition")
    .find((value) => value.textContent === term);
};

const findTerm = (term: string) => {
  return screen
    .getAllByRole("term")
    .find((value) => value.textContent === term);
};

describe("TopicSubscriptionsDetailsModal.tsx", () => {
  describe("renders correct data in details modal (Aiven)", () => {
    beforeEach(() => {
      mockGetAivenServiceAccountDetails.mockResolvedValue(
        testServiceAccountData
      );
      customRender(<TopicSubscriptionsDetailsModal {...defaultPropsAiven} />, {
        queryClient: true,
      });
    });
    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("does not fetch data for consumer offset", async () => {
      expect(mockGetConsumerOffsets).not.toHaveBeenCalled();
    });

    it("fetches the data for service account details", async () => {
      expect(mockGetAivenServiceAccountDetails).toHaveBeenCalledWith({
        aclReqNo: defaultPropsAiven.selectedSubscription.req_no,
        env: defaultPropsAiven.selectedSubscription.environment,
        serviceName: defaultPropsAiven.selectedSubscription.acl_ssl,
        topicName: defaultPropsAiven.selectedSubscription.topicname,
      });
    });

    it("renders correct data in details modal (Aiven)", async () => {
      await waitForElementToBeRemoved(screen.getByTestId("pw-skeleton"));

      expect(findTerm("Environment")).toBeVisible();
      expect(
        findDefinition(defaultPropsAiven.selectedSubscription.environmentName)
      ).toBeVisible();

      expect(findTerm("Subscription type")).toBeVisible();
      expect(
        findDefinition(
          defaultPropsAiven.selectedSubscription.topictype.toUpperCase()
        )
      ).toBeVisible();

      expect(findTerm("Pattern type")).toBeVisible();
      expect(
        findDefinition(defaultPropsAiven.selectedSubscription.aclPatternType)
      ).toBeVisible();

      expect(findTerm("Topic name")).toBeVisible();
      expect(
        findDefinition(defaultPropsAiven.selectedSubscription.topicname)
      ).toBeVisible();

      expect(findTerm("Consumer group")).toBeVisible();
      expect(
        findDefinition(defaultPropsAiven.selectedSubscription.consumergroup)
      ).toBeVisible();

      expect(findTerm("IP or Service account based")).toBeVisible();
      expect(findDefinition("Service account")).toBeVisible();

      expect(findTerm("Service account")).toBeVisible();
      expect(
        findDefinition(defaultPropsAiven.selectedSubscription.acl_ssl)
      ).toBeVisible();

      expect(findTerm("Service account password")).toBeVisible();
      expect(
        findDefinition(defaultPropsAiven.serviceAccountData.password)
      ).toBeVisible();

      expect(screen.queryByText("Consumer offset")).not.toBeInTheDocument();
    });
  });

  describe("renders correct data in details modal (non Aiven consumer)", () => {
    beforeEach(() => {
      mockGetConsumerOffsets.mockResolvedValue([testOffsetsData]);

      customRender(
        <TopicSubscriptionsDetailsModal {...defaultPropsNonAiven} />,
        {
          queryClient: true,
        }
      );
    });
    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("fetches data for consumer offset", async () => {
      expect(mockGetConsumerOffsets).toHaveBeenCalledWith({
        consumerGroupId:
          defaultPropsNonAiven.selectedSubscription.consumergroup,
        env: defaultPropsNonAiven.selectedSubscription.environment,
        topicName: defaultPropsNonAiven.selectedSubscription.topicname,
      });
    });

    it("does not fetch service account details from aiven", async () => {
      expect(mockGetAivenServiceAccountDetails).not.toHaveBeenCalled();
    });

    it("renders correct data in details modal (non Aiven consumer)", async () => {
      await waitForElementToBeRemoved(screen.getByTestId("offsets-skeleton"));

      expect(findTerm("Environment")).toBeVisible();
      expect(
        findDefinition(
          defaultPropsNonAiven.selectedSubscription.environmentName
        )
      ).toBeVisible();

      expect(findTerm("Subscription type")).toBeVisible();
      expect(
        findDefinition(
          defaultPropsNonAiven.selectedSubscription.topictype.toUpperCase()
        )
      ).toBeVisible();

      expect(findTerm("Pattern type")).toBeVisible();
      expect(
        findDefinition(defaultPropsNonAiven.selectedSubscription.aclPatternType)
      ).toBeVisible();

      expect(findTerm("Topic name")).toBeVisible();
      expect(
        findDefinition(defaultPropsNonAiven.selectedSubscription.topicname)
      ).toBeVisible();

      expect(findTerm("Consumer group")).toBeVisible();
      expect(
        findDefinition(defaultPropsNonAiven.selectedSubscription.consumergroup)
      ).toBeVisible();

      expect(findTerm("IP or Principal based")).toBeVisible();
      expect(findDefinition("IP")).toBeVisible();

      expect(findTerm("IP")).toBeVisible();
      expect(
        findDefinition(defaultPropsNonAiven.selectedSubscription.acl_ip)
      ).toBeVisible();

      expect(
        screen.getByText(
          "Partition 0 | Current offset 0 | End offset 0 | Lag 0"
        )
      ).toBeVisible();

      expect(
        screen.queryByText("Service account password")
      ).not.toBeInTheDocument();
    });
  });

  describe("should render correct data in details modal (Aiven consumer, non owner user)", () => {
    beforeAll(async () => {
      mockGetAivenServiceAccountDetails.mockResolvedValue(
        notOwnerTestServiceAccountData
      );

      customRender(
        <TopicSubscriptionsDetailsModal {...defaultPropsNotOwnerAiven} />,
        {
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByTestId("pw-skeleton"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("does not fetch data for consumer offset", async () => {
      expect(mockGetConsumerOffsets).not.toHaveBeenCalled();
    });

    it("fetches service account details from aiven", async () => {
      expect(mockGetAivenServiceAccountDetails).toHaveBeenCalledWith({
        aclReqNo: defaultPropsNotOwnerAiven.selectedSubscription.req_no,
        env: defaultPropsNotOwnerAiven.selectedSubscription.environment,
        serviceName: defaultPropsNotOwnerAiven.selectedSubscription.acl_ssl,
        topicName: defaultPropsNotOwnerAiven.selectedSubscription.topicname,
      });
    });

    it("renders correct data in details modal (Aiven consumer, non owner user)", async () => {
      expect(findTerm("Environment")).toBeVisible();
      expect(
        findDefinition(
          defaultPropsNotOwnerAiven.selectedSubscription.environmentName
        )
      ).toBeVisible();

      expect(findTerm("Subscription type")).toBeVisible();
      expect(
        findDefinition(
          defaultPropsNotOwnerAiven.selectedSubscription.topictype.toUpperCase()
        )
      ).toBeVisible();

      expect(findTerm("Pattern type")).toBeVisible();
      expect(
        findDefinition(
          defaultPropsNotOwnerAiven.selectedSubscription.aclPatternType
        )
      ).toBeVisible();

      expect(findTerm("Topic name")).toBeVisible();
      expect(
        findDefinition(defaultPropsNotOwnerAiven.selectedSubscription.topicname)
      ).toBeVisible();

      expect(findTerm("Consumer group")).toBeVisible();
      expect(
        findDefinition(
          defaultPropsNotOwnerAiven.selectedSubscription.consumergroup
        )
      ).toBeVisible();

      expect(findTerm("IP or Service account based")).toBeVisible();
      expect(findDefinition("Service account")).toBeVisible();

      expect(findTerm("Service account")).toBeVisible();
      expect(
        findDefinition(defaultPropsNotOwnerAiven.selectedSubscription.acl_ssl)
      ).toBeVisible();

      expect(findTerm("Service account password")).toBeVisible();
      expect(findDefinition("Not authorized to see this.")).toBeVisible();
    });
  });
});
