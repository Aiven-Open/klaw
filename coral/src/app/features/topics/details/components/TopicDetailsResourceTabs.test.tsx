import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TopicOverviewTabEnum } from "src/app/router_utils";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { TopicOverviewResourcesTabs } from "src/app/features/topics/details/components/TopicDetailsResourceTabs";
import { within } from "@testing-library/react/pure";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

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

describe("TopicDetailsResourceTabs", () => {
  const user = userEvent.setup();

  const testTopicName = "my-nice-topic";

  describe("shows all necessary elements", () => {
    beforeAll(() => {
      customRender(
        <TopicOverviewResourcesTabs
          topicName={testTopicName}
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
      const tablist = screen.getByRole("tablist");

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
          topicName={testTopicName}
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
