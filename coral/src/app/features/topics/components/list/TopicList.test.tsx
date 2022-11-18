import { cleanup, screen, within } from "@testing-library/react";
import { renderWithQueryClient } from "src/services/test-utils";
import { createMockTopic } from "src/domain/topic/topic-test-helper";
import TopicList from "src/app/features/topics/components/list/TopicList";

const mockedTopicNames = ["Name one", "Name two", "Name three", "Name four"];
const mockedTopics = mockedTopicNames.map((name, index) =>
  createMockTopic({ topicName: name, topicId: index })
);

describe("TopicList.tsx", () => {
  describe("shows all topics as a list", () => {
    beforeAll(() => {
      renderWithQueryClient(<TopicList topics={mockedTopics} />);
    });

    afterAll(cleanup);

    it("renders a topic list", async () => {
      const list = screen.getByRole("list", { name: "Topics" });

      expect(list).toBeVisible();
    });

    it("shows each topic as list item", async () => {
      const list = screen.getByRole("list", { name: "Topics" });

      mockedTopics.forEach((topic) => {
        const topicCard = within(list).getByText(topic.topicName);

        expect(topicCard).toBeVisible();
      });
    });
  });
});
