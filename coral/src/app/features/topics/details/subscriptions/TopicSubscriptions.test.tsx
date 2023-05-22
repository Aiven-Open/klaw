import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TopicSubscriptions from "src/app/features/topics/details/subscriptions/TopicSubscriptions";
import { getTeams } from "src/domain/team";
import { getTopicOverview } from "src/domain/topic/topic-api";
import { TopicOvervieApiResponse } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/topic/topic-api.ts");
jest.mock("src/domain/team/team-api");

const mockGetTopicOverview = getTopicOverview as jest.MockedFunction<
  typeof getTopicOverview
>;
const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useOutletContext: () => ({ topicName: "aiventopic1" }),
}));

const mockGetTopicOverviewResponse: TopicOvervieApiResponse = {
  topicExists: true,
  schemaExists: false,
  prefixAclsExists: false,
  txnAclsExists: false,
  topicInfoList: [
    {
      noOfPartitions: 1,
      noOfReplicas: "1",
      teamname: "Ospo",
      teamId: 1003,
      envId: "1",
      showEditTopic: true,
      showDeleteTopic: false,
      topicDeletable: false,
      envName: "DEV",
      topicName: "aiventopic1",
    },
  ],
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
  topicPromotionDetails: {
    topicName: "aivtopic3",
    status: "NO_PROMOTION",
  },
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
  topicIdForDocumentation: 1015,
};

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
    mockGetTopicOverview.mockResolvedValue(mockGetTopicOverviewResponse);
    mockGetTeams.mockResolvedValue(mockedTeamsResponse);
    customRender(<TopicSubscriptions />, {
      memoryRouter: true,
      queryClient: true,
    });
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
  });

  afterEach(() => {
    cleanup();
    jest.clearAllMocks();
  });

  describe("should render a table, filters and radios to switch between different kind of subscriptions", () => {
    it("should render a Table", () => {
      const table = screen.getByRole("table", {
        name: "Topic subscriptions",
      });
      expect(table).toBeVisible();
    });
    it("should render enabled and checked User subscriptions radio", () => {
      const radio = screen.getByRole("radio", {
        name: "User subs",
      });
      expect(radio).toBeVisible();
      expect(radio).toBeEnabled();
      expect(radio).toBeChecked();
    });
    it("should render enabled  Prefixed subs radio", () => {
      const radio = screen.getByRole("radio", {
        name: "Prefixed subs",
      });
      expect(radio).toBeVisible();
      expect(radio).toBeEnabled();
    });
    it("should render enabled Transactional subs radio, ", () => {
      const radio = screen.getByRole("radio", {
        name: "Transactional subs",
      });
      expect(radio).toBeVisible();
      expect(radio).toBeEnabled();
    });
    it("should render enabled Team filter", () => {
      const filter = screen.getByRole("combobox", {
        name: "Filter by team",
      });
      expect(filter).toBeVisible();
      expect(filter).toBeEnabled();
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

      await userEvent.type(search, "amathieu");

      expect(search).toHaveValue("amathieu");

      await waitFor(() => {
        const rows = screen.getAllByRole("row");
        const rowOne = screen.getByText("amathieu");
        const rowTwo = screen.queryByText("declineme");
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

  describe("should render the correct data in Table when sub radio are clicked", () => {
    it("should render User subscriptions in Table", () => {
      const radio = screen.getByRole("radio", {
        name: "User subs",
      });
      const rows = screen.getAllByRole("row");
      const rowOne = screen.getByText("aivtopic3user");
      const rowTwo = screen.getByText("declineme");
      const rowThree = screen.getByText("amathieu");
      const prefixedRow = screen.queryByText("aivendemot");
      const transactionalRow = screen.queryByText("tsttxnid");

      expect(radio).toBeChecked();
      expect(rows).toHaveLength(4);
      expect(rowOne).toBeVisible();
      expect(rowTwo).toBeVisible();
      expect(rowThree).toBeVisible();
      expect(prefixedRow).toBeNull();
      expect(transactionalRow).toBeNull();
    });

    it("should render Prefixed subscriptions in Table", async () => {
      const radio = screen.getByRole("radio", {
        name: "Prefixed subs",
      });

      await userEvent.click(radio);

      expect(radio).toBeChecked();

      const rows = screen.getAllByRole("row");
      const rowOne = screen.getByText("aivendemot");
      const userRow = screen.queryByText("amathieu");
      const transactionalRow = screen.queryByText("tsttxnid");

      expect(rows).toHaveLength(2);
      expect(rowOne).toBeVisible();
      expect(userRow).toBeNull();
      expect(transactionalRow).toBeNull();
    });

    it("should render Prefixed subscriptions in Table", async () => {
      const radio = screen.getByRole("radio", {
        name: "Transactional subs",
      });

      await userEvent.click(radio);

      expect(radio).toBeChecked();

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
});
