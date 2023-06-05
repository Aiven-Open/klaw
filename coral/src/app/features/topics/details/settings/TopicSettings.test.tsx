import { TopicSettings } from "src/app/features/topics/details/settings/TopicSettings";
import { cleanup, render, screen } from "@testing-library/react";

describe("TopicSettings", () => {
  describe("renders all necessary elements", () => {
    beforeAll(() => {
      render(<TopicSettings />);
    });

    afterAll(cleanup);

    it("shows a page headline", () => {
      const pageHeadline = screen.getByRole("heading", { name: "Settings" });

      expect(pageHeadline).toBeVisible();
    });

    it("shows a headline for the danger zone", () => {
      const dangerHeadline = screen.getByRole("heading", {
        name: "Danger zone",
      });

      expect(dangerHeadline).toBeVisible();
    });

    it("shows a headline for delete topic", () => {
      const deleteTopicHeadline = screen.getByRole("heading", {
        name: "Delete this topic",
      });

      expect(deleteTopicHeadline).toBeVisible();
    });

    it("shows a warning text about deletion of the topic", () => {
      const warningText = screen.getByText(
        "Once you delete a topic, there is no going back. Please be certain."
      );

      expect(warningText).toBeVisible();
    });

    it("shows a button to delete the topic", () => {
      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      expect(button).toBeVisible();
    });
  });
});
