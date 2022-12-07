import { cleanup, screen, render, within } from "@testing-library/react";
import { createMockTopic } from "src/domain/topic/topic-test-helper";
import TopicTable from "src/app/features/browse-topics/components/topic-table/TopicTable";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const mockedTopicNames = ["Name one", "Name two", "Name three", "Name four"];
const mockedTopics = mockedTopicNames.map((name, index) =>
  createMockTopic({ topicName: name, topicId: index })
);

const tableRowHeader = ["Topic", "Environments", "Team"];

describe("TopicTable.tsx", () => {
  describe("shows all topics as a table", () => {
    beforeAll(() => {
      mockIntersectionObserver();
      render(
        <TopicTable topics={mockedTopics} activePage={1} totalPages={2} />
      );
    });

    afterAll(cleanup);

    it("renders a topic table with information about pages", async () => {
      const table = screen.getByRole("table", {
        name: "Topics overview, page 1 of 2",
      });

      expect(table).toBeVisible();
    });

    tableRowHeader.forEach((header) => {
      it(`renders a column header for ${header}`, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 2",
        });
        const colHeader = within(table).getByRole("columnheader", {
          name: header,
        });

        expect(colHeader).toBeVisible();
      });
    });

    mockedTopics.forEach((topic) => {
      it(`renders the topic name ${topic.topicName} as row header`, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 2",
        });
        const rowHeader = within(table).getByRole("rowheader", {
          name: topic.topicName,
        });

        expect(rowHeader).toBeVisible();
      });

      it(`renders the team for ${topic.topicName} `, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 2",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${topic.topicName}`, "i"),
        });
        const team = within(row).getByRole("cell", { name: topic.teamname });

        expect(team).toBeVisible();
      });

      it(`renders a list of environments for topic ${topic}`, () => {
        const table = screen.getByRole("table", {
          name: "Topics overview, page 1 of 2",
        });
        const row = within(table).getByRole("row", {
          name: new RegExp(`${topic.topicName}`, "i"),
        });
        const environmentList = within(row).getByRole("list");
        const environments = within(environmentList).getAllByRole("listitem");

        expect(environmentList).toBeVisible();
        expect(environments).toHaveLength(topic.environmentsList.length);
      });

      topic.environmentsList.forEach((env) => {
        it(`renders environment ${env} for topic ${topic}`, () => {
          const table = screen.getByRole("table", {
            name: "Topics overview, page 1 of 2",
          });
          const row = within(table).getByRole("row", {
            name: new RegExp(`${topic.topicName}`, "i"),
          });
          const environmentList = within(row).getByRole("list");

          const environment = within(environmentList)
            .getAllByRole("listitem")
            .find((listitem) => listitem.textContent === env);

          expect(environment).toBeVisible();
        });
      });
    });
  });
});
