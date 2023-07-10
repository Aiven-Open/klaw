import { Context as AquariumContext } from "@aivenio/aquarium";
import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TopicSubscriptions from "src/app/features/topics/details/subscriptions/TopicSubscriptions";
import {
  createAclDeletionRequest,
  getAivenServiceAccountDetails,
  getConsumerOffsets,
} from "src/domain/acl/acl-api";
import { getTeams } from "src/domain/team";
import { TopicOverview } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/team/team-api");
jest.mock("src/domain/acl/acl-api.ts");

const mockGetConsumerOffsets = getConsumerOffsets as jest.MockedFunction<
  typeof getConsumerOffsets
>;
const mockGetAivenServiceAccountDetails =
  getAivenServiceAccountDetails as jest.MockedFunction<
    typeof getAivenServiceAccountDetails
  >;

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

const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;
const mockCreateDeleteAclRequest =
  createAclDeletionRequest as jest.MockedFunction<
    typeof createAclDeletionRequest
  >;

const mockAuthUserReturnValue = {
  canSwitchTeams: "",
  teamId: "1003",
  teamname: "Ospo",
  username: "Kvothe",
};

const mockAuthUser = jest.fn();
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUser(),
}));

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useOutletContext: () => ({ topicName: "aiventopic1" }),
}));

const testTopicName = "aiventopic1";
const testTopicOverview: TopicOverview = {
  topicExists: true,
  schemaExists: false,
  prefixAclsExists: false,
  txnAclsExists: false,
  topicInfo: {
    noOfPartitions: 1,
    noOfReplicas: "1",
    teamname: "Ospo",
    teamId: 1003,
    envId: "1",
    clusterId: 4,
    showEditTopic: true,
    showDeleteTopic: false,
    topicDeletable: false,
    envName: "DEV",
    topicName: testTopicName,
    hasACL: false,
    hasOpenTopicRequest: false,
    hasOpenACLRequest: false,
    highestEnv: true,
    hasOpenRequest: false,
    hasSchema: false,
    description: "my description",
  },
  aclInfoList: [
    {
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
    },
    {
      req_no: "1011",
      acl_ssl: "declineme",
      topicname: "aivtopic3",
      topictype: "Consumer",
      consumergroup: "-na-",
      environment: "1",
      environmentName: "DEV",
      teamname: "Ospo",
      teamid: 1003,
      aclPatternType: "LITERAL",
      showDeleteAcl: true,
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
    },
    {
      req_no: "1060",
      acl_ssl: "amathieu",
      topicname: "aivtopic3",
      topictype: "Consumer",
      consumergroup: "-na-",
      environment: "1",
      environmentName: "DEV",
      teamname: "DevRel",
      teamid: 1004,
      aclPatternType: "LITERAL",
      showDeleteAcl: true,
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
    },
  ],
  prefixedAclInfoList: [
    {
      req_no: "1063",
      aclPatternType: "PREFIXED",
      environment: "2",
      environmentName: "TST",
      kafkaFlavorType: "APACHE_KAFKA",
      showDeleteAcl: true,
      teamid: 1003,
      teamname: "Ospo",
      topicname: "aivendemot",
      topictype: "Producer",
      acl_ssl: "CN=myhosttest,OU=IS,OU=OU,OU=Services,O=Org",
    },
  ],
  transactionalAclInfoList: [
    {
      req_no: "1064",
      aclPatternType: "LITERAL",
      environment: "2",
      environmentName: "TST",
      kafkaFlavorType: "APACHE_KAFKA",
      showDeleteAcl: true,
      teamid: 1003,
      teamname: "Ospo",
      topicname: "aivendemotopic",
      topictype: "Producer",
      acl_ip: "32.121.67.2",
      acl_ssl: "User:*",
      transactionalId: "tsttxnid",
    },
  ],
  topicHistoryList: [
    {
      environmentName: "DEV",
      teamName: "Ospo",
      requestedBy: "muralibasani",
      requestedTime: "2022-Nov-04 14:41:18",
      approvedBy: "josepprat",
      approvedTime: "2022-Nov-04 14:48:38",
      remarks: "Create",
    },
  ],
  availableEnvironments: [
    {
      id: "1",
      name: "DEV",
    },
    {
      id: "2",
      name: "TST",
    },
  ],
  topicPromotionDetails: { status: "STATUS" },
  topicIdForDocumentation: 1,
};

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useOutletContext: () => ({
    topicOverview: testTopicOverview,
    environmentId: "333",
  }),
}));

const mockedTeamsResponse = [
  {
    teamname: "Ospo",
    teammail: "ospo@aiven.io",
    teamphone: "003157843623",
    contactperson: "Ospo Office",
    tenantId: 101,
    teamId: 1003,
    app: "",
    showDeleteTeam: false,
    tenantName: "default",
    envList: ["ALL"],
  },
  {
    teamname: "DevRel",
    teammail: "devrel@aiven.io",
    teamphone: "003146237478",
    contactperson: "Dev Rel",
    tenantId: 101,
    teamId: 1004,
    app: "",
    showDeleteTeam: false,
    tenantName: "default",
    envList: ["ALL"],
  },
];

describe("TopicSubscriptions.tsx", () => {
  beforeEach(async () => {
    mockIntersectionObserver();
    mockGetTeams.mockResolvedValue(mockedTeamsResponse);
    mockAuthUser.mockReturnValue(mockAuthUserReturnValue);

    customRender(
      // Aquarium context is needed for useToast
      <AquariumContext>
        <TopicSubscriptions />
      </AquariumContext>,
      {
        memoryRouter: true,
        queryClient: true,
      }
    );
    await waitForElementToBeRemoved(screen.getByTestId("select-team-loading"));
  });

  afterEach(() => {
    cleanup();
    jest.clearAllMocks();
  });

  describe("should render boxes with Consumers and Producers stats", () => {
    it("should render a Producers box with correct data", async () => {
      const producersBox = screen.getByTitle(
        "Amount of producer subscriptions"
      );
      expect(producersBox).toBeVisible();

      const number = within(producersBox).getByText("3");
      const name = within(producersBox).getByText("Producers");

      expect(number).toBeVisible();
      expect(name).toBeVisible();
    });

    it("should render a Consumers box with correct data", async () => {
      const consumersBox = screen.getByTitle(
        "Amount of consumer subscriptions"
      );

      expect(consumersBox).toBeVisible();

      const number = within(consumersBox).getByText("2");
      const name = within(consumersBox).getByText("Consumers");

      expect(number).toBeVisible();
      expect(name).toBeVisible();
    });
  });

  describe("should render a table and buttons to switch between different kind of subscriptions", () => {
    it("should render a Table", () => {
      const table = screen.getByRole("table", {
        name: "Topic subscriptions",
      });
      expect(table).toBeVisible();
    });
    it("should render enabled and checked User subscriptions button, ", () => {
      const button = screen.getByRole("button", {
        name: "User subs.",
      });
      expect(button).toBeVisible();
      expect(button).toBeEnabled();
    });
    it("should render enabled  Prefixed subs button, ", () => {
      const button = screen.getByRole("button", {
        name: "Prefixed subs.",
      });
      expect(button).toBeVisible();
      expect(button).toBeEnabled();
    });
    it("should render enabled Transactional subs button, ", () => {
      const button = screen.getByRole("button", {
        name: "Transactional subs.",
      });
      expect(button).toBeVisible();
      expect(button).toBeEnabled();
    });
    it("should render enabled Team filter with default value", () => {
      const filter = screen.getByRole("combobox", {
        name: "Filter by team",
      });
      expect(filter).toBeVisible();
      expect(filter).toBeEnabled();
      expect(filter).toHaveValue("1003");
    });
    it("should render enabled ACL type filter", () => {
      const filter = screen.getByRole("combobox", {
        name: "Filter by ACL type",
      });
      expect(filter).toBeVisible();
      expect(filter).toBeEnabled();
    });
    it("should render search filter", () => {
      const search = screen.getByRole("search");
      expect(search).toBeVisible();
      expect(search).toBeEnabled();
    });
  });

  describe("should render the correct data in Table when interacting with filters", () => {
    it("should filter by Team", async () => {
      const filter = screen.getByRole("combobox", {
        name: "Filter by team",
      });
      const devRelOption = within(filter).getByRole("option", {
        name: "DevRel",
      });

      await userEvent.selectOptions(filter, devRelOption);

      const rows = screen.getAllByRole("row");
      const rowOne = screen.getByText("amathieu");
      const rowTwo = screen.queryByText("declineme");
      const rowThree = screen.queryByText("aivtopic3user");
      const prefixedRow = screen.queryByText("aivendemot");
      const transactionalRow = screen.queryByText("tsttxnid");

      expect(filter).toHaveValue("1004");
      expect(rows).toHaveLength(2);
      expect(rowOne).toBeVisible();
      expect(rowTwo).toBeNull();
      expect(rowThree).toBeNull();
      expect(prefixedRow).toBeNull();
      expect(transactionalRow).toBeNull();
    });

    it("should filter by ACL type", async () => {
      const filter = screen.getByRole("combobox", {
        name: "Filter by ACL type",
      });
      const producerOption = within(filter).getByRole("option", {
        name: "PRODUCER",
      });

      await userEvent.selectOptions(filter, producerOption);

      const rows = screen.getAllByRole("row");
      const rowOne = screen.getByText("aivtopic3user");
      const rowTwo = screen.queryByText("declineme");
      const rowThree = screen.queryByText("amathieu");
      const prefixedRow = screen.queryByText("aivendemot");
      const transactionalRow = screen.queryByText("tsttxnid");

      expect(filter).toHaveValue("PRODUCER");
      expect(rows).toHaveLength(2);
      expect(rowOne).toBeVisible();
      expect(rowTwo).toBeNull();
      expect(rowThree).toBeNull();
      expect(prefixedRow).toBeNull();
      expect(transactionalRow).toBeNull();
    });

    it("should allow searching", async () => {
      const search = screen.getByRole("search");

      await userEvent.type(search, "declineme");

      expect(search).toHaveValue("declineme");

      await waitFor(() => {
        const rows = screen.getAllByRole("row");
        const rowOne = screen.getByText("declineme");
        const rowTwo = screen.queryByText("amathieu");
        const rowThree = screen.queryByText("aivtopic3user");
        const prefixedRow = screen.queryByText("aivendemot");
        const transactionalRow = screen.queryByText("tsttxnid");
        expect(rows).toHaveLength(2);
        expect(rowOne).toBeVisible();
        expect(rowTwo).toBeNull();
        expect(rowThree).toBeNull();
        expect(prefixedRow).toBeNull();
        expect(transactionalRow).toBeNull();
      });
    });
  });

  describe("should render the correct data in Table when sub button are clicked", () => {
    it("should render User subscriptions in Table by default, filtered by current user Team", () => {
      const rows = screen.getAllByRole("row");
      const rowOne = screen.getByText("declineme");
      const rowTwo = screen.getByText("aivtopic3user");
      const rowThree = screen.queryByText("amathieu");
      const prefixedRow = screen.queryByText("aivendemot");
      const transactionalRow = screen.queryByText("tsttxnid");

      expect(rows).toHaveLength(3);
      expect(rowOne).toBeVisible();
      expect(rowTwo).toBeVisible();
      expect(rowThree).toBeNull();
      expect(prefixedRow).toBeNull();
      expect(transactionalRow).toBeNull();
    });

    it("should render Prefixed subscriptions in Table, ", async () => {
      const button = screen.getByRole("button", {
        name: "Prefixed subs.",
      });

      await userEvent.click(button);

      expect(button).toHaveFocus();

      const rows = screen.getAllByRole("row");
      const rowOne = screen.getByText("aivendemot");
      const userRow = screen.queryByText("amathieu");
      const transactionalRow = screen.queryByText("tsttxnid");

      expect(rows).toHaveLength(2);
      expect(rowOne).toBeVisible();
      expect(userRow).toBeNull();
      expect(transactionalRow).toBeNull();
    });

    it("should render Prefixed subscriptions in Table, ", async () => {
      const button = screen.getByRole("button", {
        name: "Transactional subs.",
      });

      await userEvent.click(button);

      expect(button).toHaveFocus();

      const rows = screen.getAllByRole("row");
      const rowOne = screen.getByText("tsttxnid");
      const prefixedRow = screen.queryByText("aivendemot");
      const userRow = screen.queryByText("amathieu");

      expect(rows).toHaveLength(2);
      expect(rowOne).toBeVisible();
      expect(userRow).toBeNull();
      expect(prefixedRow).toBeNull();
    });
  });

  describe("should allow creating a delete request for a subscription", () => {
    it("should render a modal when clicking the Delete button and close it when clicking Cancel button", async () => {
      const firstDataRow = screen.getAllByRole("row")[1];
      const button = within(firstDataRow).getByRole("button", {
        name: "Create deletion request for request 1006",
      });

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();

      const cancelButton = within(modal).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.click(cancelButton);

      await waitFor(() => expect(modal).not.toBeVisible());
    });

    it("should render a modal when clicking the Delete button and create a delete request when clicking the modal's Create button", async () => {
      const firstDataRow = screen.getAllByRole("row")[1];
      const button = within(firstDataRow).getByRole("button", {
        name: "Create deletion request for request 1006",
      });

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");
      const createButton = within(modal).getByRole("button", {
        name: "Create deletion request",
      });

      await userEvent.click(createButton);

      expect(mockCreateDeleteAclRequest).toHaveBeenNthCalledWith(1, {
        requestId: "1006",
      });

      await waitFor(() => expect(modal).not.toBeVisible());
      await waitFor(() =>
        expect(
          screen.getByText("Subscription deletion request successfully created")
        ).toBeVisible()
      );
    });
  });

  describe("should allow seeing details of a subscription", () => {
    beforeAll(() => {
      mockGetConsumerOffsets.mockResolvedValue([testOffsetsData]);
      mockGetAivenServiceAccountDetails.mockResolvedValue(
        testServiceAccountData
      );
    });
    it("should render a modal when clicking the Details button and close it when clicking Close button", async () => {
      const firstDataRow = screen.getAllByRole("row")[1];
      const button = within(firstDataRow).getByRole("button", {
        name: "Show details of request 1006",
      });

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");

      expect(within(modal).getByText("Subscription details")).toBeVisible();

      const cancelButton = within(modal).getByRole("button", {
        name: "Close",
      });

      await userEvent.click(cancelButton);

      await waitFor(() => expect(modal).not.toBeVisible());
    });
  });
});
