import { TopicSettings } from "src/app/features/topics/details/settings/TopicSettings";
import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("TopicSettings", () => {
  const user = userEvent.setup();
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

  describe("enables user to delete a topic", () => {
    beforeEach(() => {
      render(<TopicSettings />);
    });

    afterEach(cleanup);

    it('shows a confirmation modal when user clicks "Delete topic"', async () => {
      const confirmationModalBeforeClick = screen.queryByRole("dialog");
      expect(confirmationModalBeforeClick).not.toBeInTheDocument();

      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);
      const confirmationModal = screen.getByRole("dialog");
      expect(confirmationModal).toBeVisible();
    });

    it("shows dialog with more information to delete topic", async () => {
      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);

      const headline = within(screen.getByRole("dialog")).getByRole("heading", {
        name: "Delete topic",
      });
      const text = within(screen.getByRole("dialog")).getByText(
        "Are you sure you want to delete this topic?"
      );

      expect(headline).toBeVisible();
      expect(text).toBeVisible();
    });

    it("shows dialog with option to delete topic or cancel process", async () => {
      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);

      const cancelButton = within(screen.getByRole("dialog")).getByRole(
        "button",
        {
          name: "Cancel",
        }
      );

      const deleteButton = within(screen.getByRole("dialog")).getByRole(
        "button",
        {
          name: "Delete topic",
        }
      );

      expect(cancelButton).toBeEnabled();
      expect(deleteButton).toBeEnabled();
    });

    it('removes modal and does not delete topic if user clicks "cancel"', async () => {
      const button = screen.getByRole("button", {
        name: "Delete topic",
      });

      await user.click(button);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const cancelButton = within(dialog).getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      expect(dialog).not.toBeInTheDocument();
    });
  });
});
