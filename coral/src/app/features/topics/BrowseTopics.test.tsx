import { cleanup, screen } from "@testing-library/react";
import { server } from "src/services/api-mocks/server";
import { renderWithQueryClient } from "src/services/test-utils";
import {
  mockTopicGetRequest,
  mockedResponseTransformed,
} from "src/domain/topics/topics-api.msw";
import BrowseTopics from "src/app/features/topics/BrowseTopics";
import { waitForElementToBeRemoved, within } from "@testing-library/react/pure";

describe("TopicList.tsx", () => {
  beforeAll(() => {
    server.listen();
  });

  beforeEach(() => {
    mockTopicGetRequest({ mswInstance: server });
    renderWithQueryClient(<BrowseTopics />);
  });

  afterAll(() => {
    server.close();
  });

  afterEach(() => {
    server.resetHandlers();
    cleanup();
  });

  it("shows the topic list when topics are loaded", async () => {
    await waitForElementToBeRemoved(screen.getByText("Loading..."));
    const list = screen.getByRole("list");

    expect(list).toBeVisible();
  });

  it("shows the topics as list item", async () => {
    await waitForElementToBeRemoved(screen.getByText("Loading..."));
    const list = screen.getByRole("list");

    const topic = within(list).getByRole("heading", {
      name: mockedResponseTransformed[0].topicName,
    });

    expect(topic).toBeVisible();
  });
});
