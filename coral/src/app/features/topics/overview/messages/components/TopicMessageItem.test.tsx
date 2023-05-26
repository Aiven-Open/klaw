import { cleanup, render, screen } from "@testing-library/react";
import { TopicMessageItem } from "src/app/features/topics/overview/messages/components/TopicMessageItem";
import userEvent from "@testing-library/user-event";

describe("TopicMessageItem", () => {
  afterEach(() => {
    cleanup();
  });
  it("displays the message", () => {
    render(<TopicMessageItem message="Hello World" offsetId="0" />);
    screen.getByText("Hello World");
  });
  it("indicates if the message has no content", () => {
    render(<TopicMessageItem message="" offsetId="0" />);
    screen.getByText("Empty message");
    expect(screen.getByLabelText("Expand message 0")).toBeDisabled();
  });
  it("truncates the message at 100 characters", () => {
    render(<TopicMessageItem message={"H".repeat(101)} offsetId="0" />);
    screen.getByText("H".repeat(97) + "...");
  });
  it("display the message in full if expand button is pressed", async () => {
    render(<TopicMessageItem message={"H".repeat(101)} offsetId="0" />);
    await userEvent.click(screen.getByLabelText("Expand message 0"));
    screen.getByText("H".repeat(101));
  });
});
