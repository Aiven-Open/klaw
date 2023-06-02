import { cleanup, screen, within } from "@testing-library/react";
import TopicTable from "src/app/features/topics/browse/components/TopicTable";
import { createMockTopic } from "src/domain/topic/topic-test-helper";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabThroughForward } from "src/services/test-utils/tabbing";

const mockedTopicNames = ["Name-one", "Name-two", "Name-three", "Name-four"];
const mockedTopics = mockedTopicNames.map((name, index) =>
  createMockTopic({ topicName: name, topicId: index })
);

const isFeatureFlagActiveMock = jest.fn();
jest.mock("src/services/feature-flags/utils", () => ({
  isFeatureFlagActive: () => isFeatureFlagActiveMock(),
}));

const tableRowHeader = ["Topic", "Environments", "Team"];

describe("TopicTable.tsx", () => {
  describe("shows empty state correctly", () => {
    afterAll(cleanup);

    it("show empty state when there is no data", () => {
      customRender(
        <TopicTable topics={[]} ariaLabel={"Topics overview, page 0 of 0"} />,
        { memoryRouter: true }
      );
      expect(screen.getByRole("heading", { name: "No Topics" })).toBeVisible();
    });
  });

  describe("shows all topics as a table", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      isFeatureFlagActiveMock.mockResolvedValue(true);
      customRender(
        <TopicTable
          topics={mockedTopics}
          ariaLabel={"Topics overview, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("renders a topic table with information about pages", async () => {
      const table = screen.getByRole("table", {
        name: "Topics overview, page 1 of 10",
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 10",
        });
        const colHeader = within(table).getByRole("columnheader", {
          name: header,
        });

        expect(colHeader).toBeVisible();
      });
    });

    mockedTopics.forEach((topic) => {
      it(`renders the topic name "${topic.topicName}" as a link to the detail view as row header`, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 10",
        });
        const rowHeader = within(table).getByRole("cell", {
          name: topic.topicName,
        });
        const link = within(rowHeader).getByRole("link", {
          name: topic.topicName,
        });

        expect(rowHeader).toBeVisible();
        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          `/topic/${topic.topicName}/overview`
        );
      });

      it(`renders the team for ${topic.topicName} `, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${topic.topicName}`, "i"),
        });
        const team = within(row).getByRole("cell", { name: topic.teamname });

        expect(team).toBeVisible();
      });

      it(`renders a list of Environments for topic ${topic}`, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 10",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${topic.topicName}`, "i"),
        });
        const environmentList = within(row).getByRole("cell", {
          name: topic.environmentsList.join(" "),
        });

        expect(environmentList).toBeVisible();
      });

      topic.environmentsList.forEach((env) => {
        it(`renders Environment ${env} for topic ${topic}`, () => {
          const table = screen.getByRole("table", {
            name: "Topics overview, page 1 of 10",
          });
          const row = within(table).getByRole("row", {
            name: new RegExp(`${topic.topicName}`, "i"),
          });
          const environmentList = within(row).getByRole("cell", {
            name: topic.environmentsList.join(" "),
          });

          expect(environmentList).toBeVisible();
        });
      });
    });
  });

  describe("renders link to angular app if feature-flag is not enabled", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      isFeatureFlagActiveMock.mockReturnValue(false);
      customRender(
        <TopicTable
          topics={mockedTopics}
          ariaLabel={"Topics overview, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    mockedTopics.forEach((topic) => {
      it(`renders the topic name "${topic.topicName}" as a link to the detail view as row header`, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 10",
        });
        const rowHeader = within(table).getByRole("cell", {
          name: topic.topicName,
        });
        const link = within(rowHeader).getByRole("link", {
          name: topic.topicName,
        });

        expect(rowHeader).toBeVisible();
        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          `http://localhost/topicOverview?topicname=${topic.topicName}`
        );
      });
    });
  });

  describe("enables user to keyboard navigate from topic name to topic name", () => {
    beforeEach(() => {
      mockIntersectionObserver();
      customRender(
        <TopicTable
          topics={mockedTopics}
          ariaLabel={"Topics overview, page 1 of 10"}
        />,
        { memoryRouter: true }
      );
      const table = screen.getByRole("table", {
        name: "Topics overview, page 1 of 10",
      });
      table.focus();
    });

    afterEach(cleanup);

    mockedTopics.forEach((topic, index) => {
      const numbersOfTabs = index + 1;
      it(`sets focus on "${topic.topicName}" when user tabs ${numbersOfTabs} times`, async () => {
        const link = screen.getByRole("link", {
          name: topic.topicName,
        });

        expect(link).not.toHaveFocus();

        await tabThroughForward(numbersOfTabs);

        expect(link).toHaveFocus();
      });
    });
  });
});
