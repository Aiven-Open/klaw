import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TopicOverviewTabEnum } from "src/app/router_utils";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/details/components/TopicDetailsResourceTabs";
import { within } from "@testing-library/react/pure";
import { TopicOverview } from "src/domain/topic";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const mockSetSchemaVersion = jest.fn();

const testMapTabs = [
  {
    linkTo: "overview",
    title: "Overview",
  },
  {
    linkTo: "subscriptions",
    title: "Subscriptions",
  },
  {
    linkTo: "messages",
    title: "Messages",
  },
  {
    linkTo: "schema",
    title: "Schema",
  },
  {
    linkTo: "documentation",
    title: "Documentation",
  },
  {
    linkTo: "history",
    title: "History",
  },
  {
    linkTo: "settings",
    title: "Settings",
  },
];

const testTopicName = "my-nice-topic";

const testTopicOverview: TopicOverview = {
  topicExists: true,
  schemaExists: false,
  prefixAclsExists: false,
  txnAclsExists: false,
  topicInfo: {
    topicName: testTopicName,
    noOfPartitions: 1,
    noOfReplicas: "1",
    teamname: "Ospo",
    teamId: 0,
    envId: "1",
    showEditTopic: true,
    showDeleteTopic: false,
    topicDeletable: false,
    envName: "DEV",
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
  topicPromotionDetails: { status: "SUCCESS" },
  topicIdForDocumentation: 1,
};

const defaultProps = {
  currentTab: TopicOverviewTabEnum.OVERVIEW,
  environmentId: "1",
  isError: false,
  isLoading: false,
  topicName: testTopicName,
  topicOverview: testTopicOverview,
  setSchemaVersion: mockSetSchemaVersion,
  topicOverviewIsRefetching: false,
  topicSchemasIsRefetching: false,
};
describe("TopicDetailsResourceTabs", () => {
  const user = userEvent.setup();

  describe("handles error state", () => {
    beforeAll(() => {
      customRender(
        <TopicOverviewResourcesTabs {...defaultProps} isError={true} />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows an alert", () => {
      const alert = screen.getByRole("alert");

      expect(alert).toBeVisible();
    });

    it("shows an error message", () => {
      const alert = screen.getByRole("alert");

      expect(alert).toHaveTextContent(
        "There was an error trying to load the topic details"
      );
    });

    it("does not show content for active tab navigation", () => {
      const tablist = screen.queryByTestId("tabpanel-content");

      expect(tablist).not.toBeInTheDocument();
    });
  });

  describe("handles loading state", () => {
    beforeAll(() => {
      customRender(
        <TopicOverviewResourcesTabs {...defaultProps} isLoading={true} />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a loading information", () => {
      const loading = screen.getByText("Loading topic details");

      expect(loading).toBeVisible();
    });

    it("does not show content for active tab navigation", () => {
      const tablist = screen.queryByTestId("tabpanel-content");

      expect(tablist).not.toBeInTheDocument();
    });
  });

  describe("handles a non-existent topic", () => {
    beforeAll(() => {
      customRender(
        <TopicOverviewResourcesTabs
          {...defaultProps}
          topicOverview={{
            ...testTopicOverview,
            topicExists: false,
          }}
        />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows an alert", () => {
      const alert = screen.getByRole("alert");

      expect(alert).toBeVisible();
    });

    it("shows an information about non-existing topic", () => {
      const alert = screen.getByRole("alert");

      expect(alert).toHaveTextContent(`Topic ${testTopicName} does not exist.`);
    });

    it("does not show content for active tab navigation", () => {
      const tablist = screen.queryByTestId("tabpanel-content");

      expect(tablist).not.toBeInTheDocument();
    });
  });

  describe("renders the detail page for topic", () => {
    beforeAll(() => {
      customRender(
        <TopicOverviewResourcesTabs
          {...defaultProps}
          currentTab={TopicOverviewTabEnum.OVERVIEW}
        />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders a tablist", () => {
      const tablistWrapper = screen.getByTestId("tabpanel-content");
      const tablist = screen.getByRole("tablist");

      expect(tablistWrapper).toBeVisible();
      expect(tablist).toBeVisible();
    });

    testMapTabs.forEach((tab) => {
      const name = tab.title;
      it(`shows a tab element "${name}"`, () => {
        const tab = screen.getByRole("tab", { name: name });

        expect(tab).toBeVisible();
      });

      it(`renders a button as the "${name}" tab element`, () => {
        const tab = screen.getByRole("tab", { name: name });

        expect(tab.tagName).toBe("BUTTON");
      });

      it(`adds information which part of the panel the button "${name}" controls`, () => {
        const tab = screen.getByRole("tab", { name: name });

        expect(tab).toHaveAttribute(
          "aria-controls",
          `${name.toLowerCase()}-panel`
        );
      });
    });

    it("shows which tab is currently selected based on a prop", () => {
      const tab = screen.getByRole("tab", { selected: true });

      expect(tab).toHaveAccessibleName("Overview");
    });

    it("shows a preview banner to enables users to go back to original app", () => {
      const banner = screen.getByRole("region", { name: "Preview disclaimer" });
      const link = within(banner).getByRole("link", { name: "old interface" });

      expect(banner).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        `/topicOverview?topicname=${testTopicName}`
      );
    });
  });

  describe("enables users to switch panels", () => {
    beforeEach(() => {
      customRender(
        <TopicOverviewResourcesTabs
          {...defaultProps}
          currentTab={TopicOverviewTabEnum.OVERVIEW}
        />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    testMapTabs.forEach((tab) => {
      const name = tab.title;
      const linkTo = tab.linkTo;

      it(`navigates to correct URL when ${name} tab is clicked`, async () => {
        const tab = screen.getByRole("tab", { name: name });
        await user.click(tab);
        expect(mockedNavigate).toHaveBeenCalledWith(
          `/topic/${testTopicName}/${linkTo}`,
          {
            replace: true,
          }
        );
      });
    });
  });
});
