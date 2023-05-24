import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TopicSubscriptions from "src/app/features/topics/details/subscriptions/TopicSubscriptions";
import { createAclDeletionRequest } from "src/domain/acl/acl-api";
import { getTopicOverview } from "src/domain/topic/topic-api";
import { TopicOvervieApiResponse } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Context as AquariumContext } from "@aivenio/aquarium";

jest.mock("src/domain/topic/topic-api.ts");
jest.mock("src/domain/acl/acl-api.ts");

const mockGetTopicOverview = getTopicOverview as jest.MockedFunction<
  typeof getTopicOverview
>;
const mockCreateDeleteAclRequest =
  createAclDeletionRequest as jest.MockedFunction<
    typeof createAclDeletionRequest
  >;

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useOutletContext: () => ({ topicName: "aiventopic1" }),
}));

jest.mock("crypto", () => ({
  randomUUID: () => null,
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
      teamId: 0,
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
      teamid: 0,
      aclPatternType: "LITERAL",
      showDeleteAcl: true,
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
    },
    {
      req_no: "1011",
      acl_ssl: "declineme",
      topicname: "aivtopic3",
      topictype: "Producer",
      consumergroup: "-na-",
      environment: "1",
      environmentName: "DEV",
      teamname: "Ospo",
      teamid: 0,
      aclPatternType: "LITERAL",
      showDeleteAcl: true,
      kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
    },
    {
      req_no: "1060",
      acl_ssl: "amathieu",
      topicname: "aivtopic3",
      topictype: "Producer",
      consumergroup: "-na-",
      environment: "1",
      environmentName: "DEV",
      teamname: "Ospo",
      teamid: 0,
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

describe("TopicSubscriptions.tsx", () => {
  beforeAll(async () => {
    mockIntersectionObserver();
    mockGetTopicOverview.mockResolvedValue(mockGetTopicOverviewResponse);
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
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
  });

  afterAll(() => {
    cleanup();
    jest.clearAllMocks();
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
  });

  describe("should render the correct data in Table when sub button are clicked", () => {
    it("should render User subscriptions in Table, ", () => {
      const rows = screen.getAllByRole("row");
      const rowOne = screen.getByText("aivtopic3user");
      const rowTwo = screen.getByText("declineme");
      const rowThree = screen.getByText("amathieu");
      const prefixedRow = screen.queryByText("aivendemot");
      const transactionalRow = screen.queryByText("tsttxnid");

      expect(rows).toHaveLength(4);
      expect(rowOne).toBeVisible();
      expect(rowTwo).toBeVisible();
      expect(rowThree).toBeVisible();
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
        name: "Create deletion request for request 1064",
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
        name: "Create deletion request for request 1064",
      });

      await userEvent.click(button);

      const modal = screen.getByRole("dialog");
      const createButton = within(modal).getByRole("button", {
        name: "Create deletion request",
      });

      await userEvent.click(createButton);

      expect(mockCreateDeleteAclRequest).toHaveBeenCalledTimes(1);

      await waitFor(() => expect(modal).not.toBeVisible());
      await waitFor(() =>
        expect(
          screen.getByText(
            "Subscription deletion request successfully created."
          )
        ).toBeVisible()
      );
    });
  });
});
