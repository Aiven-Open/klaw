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
import { AuthUser } from "src/domain/auth-user";
import { AclOverviewInfo } from "src/domain/topic/topic-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api");
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUser(),
}));

const mockGetConsumerOffsets = getConsumerOffsets as jest.MockedFunction<
  typeof getConsumerOffsets
>;
const mockGetAivenServiceAccountDetails =
  getAivenServiceAccountDetails as jest.MockedFunction<
    typeof getAivenServiceAccountDetails
  >;
const mockAuthUser = jest.fn();
const mockCloseDetailsModal = jest.fn();

const authUser: AuthUser = {
  canSwitchTeams: "false",
  teamId: "2",
  teamname: "awesome-bunch-of-people",
  username: "i-am-test-user",
};

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

const defaultPropsAiven = {
  closeDetailsModal: mockCloseDetailsModal,
  isAivenCluster: true,
  selectedSub: testSelectedSubAiven,
  serviceAccountData: testServiceAccountData,
};

const defaultPropsNonAiven = {
  closeDetailsModal: mockCloseDetailsModal,
  isAivenCluster: false,
  selectedSub: testSelectedSubNonAiven,
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
  beforeAll(() => {
    mockAuthUser.mockReturnValue(authUser);
  });

  afterEach(() => {
    cleanup();
  });

  it("should render correct data in details modal (Aiven)", async () => {
    mockGetAivenServiceAccountDetails.mockResolvedValue(testServiceAccountData);

    customRender(<TopicSubscriptionsDetailsModal {...defaultPropsAiven} />, {
      queryClient: true,
    });

    await waitForElementToBeRemoved(screen.getByTestId("pw-skeleton"));

    expect(findTerm("Environment")).toBeVisible();
    expect(
      findDefinition(defaultPropsAiven.selectedSub.environmentName)
    ).toBeVisible();

    expect(findTerm("Subscription type")).toBeVisible();
    expect(
      findDefinition(defaultPropsAiven.selectedSub.topictype.toUpperCase())
    ).toBeVisible();

    expect(findTerm("Pattern type")).toBeVisible();
    expect(
      findDefinition(defaultPropsAiven.selectedSub.aclPatternType)
    ).toBeVisible();

    expect(findTerm("Topic name")).toBeVisible();
    expect(
      findDefinition(defaultPropsAiven.selectedSub.topicname)
    ).toBeVisible();

    expect(findTerm("Consumer group")).toBeVisible();
    expect(
      findDefinition(defaultPropsAiven.selectedSub.consumergroup)
    ).toBeVisible();

    expect(findTerm("IP or Service account based")).toBeVisible();
    expect(findDefinition("Service account")).toBeVisible();

    expect(findTerm("Service account")).toBeVisible();
    expect(findDefinition(defaultPropsAiven.selectedSub.acl_ssl)).toBeVisible();

    expect(findTerm("Service account password")).toBeVisible();
    expect(
      findDefinition(defaultPropsAiven.serviceAccountData.password)
    ).toBeVisible();

    expect(screen.queryByText("Consumer offset")).not.toBeInTheDocument();
  });

  it("should render correct data in details modal (non Aiven consumer)", async () => {
    mockGetConsumerOffsets.mockResolvedValue([testOffsetsData]);

    customRender(<TopicSubscriptionsDetailsModal {...defaultPropsNonAiven} />, {
      queryClient: true,
    });

    await waitForElementToBeRemoved(screen.getByTestId("offsets-skeleton"));

    expect(findTerm("Environment")).toBeVisible();
    expect(
      findDefinition(defaultPropsNonAiven.selectedSub.environmentName)
    ).toBeVisible();

    expect(findTerm("Subscription type")).toBeVisible();
    expect(
      findDefinition(defaultPropsNonAiven.selectedSub.topictype.toUpperCase())
    ).toBeVisible();

    expect(findTerm("Pattern type")).toBeVisible();
    expect(
      findDefinition(defaultPropsNonAiven.selectedSub.aclPatternType)
    ).toBeVisible();

    expect(findTerm("Topic name")).toBeVisible();
    expect(
      findDefinition(defaultPropsNonAiven.selectedSub.topicname)
    ).toBeVisible();

    expect(findTerm("Consumer group")).toBeVisible();
    expect(
      findDefinition(defaultPropsNonAiven.selectedSub.consumergroup)
    ).toBeVisible();

    expect(findTerm("IP or Principal based")).toBeVisible();
    expect(findDefinition("IP")).toBeVisible();

    expect(findTerm("IP")).toBeVisible();
    expect(
      findDefinition(defaultPropsNonAiven.selectedSub.acl_ip)
    ).toBeVisible();

    expect(
      screen.getByText("Partition 0 | Current offset 0 | End offset 0 | Lag 0")
    ).toBeVisible();

    expect(
      screen.queryByText("Service account password")
    ).not.toBeInTheDocument();
  });
});
