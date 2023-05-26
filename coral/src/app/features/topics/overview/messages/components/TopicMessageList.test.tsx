import { cleanup, render, screen } from "@testing-library/react";
import { TopicMessageList } from "src/app/features/topics/overview/messages/components/TopicMessageList";

const messages = {
  0: "HELLO",
  1: "WORLD",
};

describe("TopicMessageList", () => {
  afterEach(() => {
    cleanup();
  });
  it("displays all messages", () => {
    render(<TopicMessageList messages={messages} />);
    screen.getByText("HELLO");
    screen.getByText("WORLD");
  });
});
