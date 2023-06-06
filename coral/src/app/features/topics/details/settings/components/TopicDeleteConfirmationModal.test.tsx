import { TopicDeleteConfirmationModal } from "src/app/features/topics/details/settings/components/TopicDeleteConfirmationModal";
import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mockOnClose = jest.fn();
const mockOnSubmit = jest.fn();
describe("TopicDeleteConfirmationModal", () => {
  const user = userEvent.setup();

  describe("renders all necessary elements when isLoading is false", () => {
    beforeAll(() => {
      render(
        <TopicDeleteConfirmationModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
        />
      );
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a dialog element", () => {
      const dialog = screen.getByRole("dialog");

      expect(dialog).toBeVisible();
    });

    it("shows more information to delete the topic", () => {
      const dialog = screen.getByRole("dialog");
      const headline = within(dialog).getByRole("heading", {
        name: "Delete topic",
      });
      const text = within(dialog).getByText(
        "Are you sure you would like to delete this topic? Once this request is made it cannot be reversed."
      );

      expect(headline).toBeVisible();
      expect(text).toBeVisible();
    });

    it("shows a checkbox to delete associated schema", () => {
      const dialog = screen.getByRole("dialog");
      const checkbox = within(dialog).getByRole("checkbox", {
        name: "Delete all versions of schema associated with this topic if a schema exists.",
      });

      expect(checkbox).toBeEnabled();
      expect(checkbox).not.toBeChecked();
    });

    it("shows a textarea where user can add a comment why they delete the topic", () => {
      const dialog = screen.getByRole("dialog");
      const textarea = within(dialog).getByRole("textbox", {
        name: "You can add the reason to delete the topic (optional)",
      });

      expect(textarea).toBeEnabled();
    });

    it("shows a button to cancel process", () => {
      const dialog = screen.getByRole("dialog");
      const cancelButton = within(dialog).getByRole("button", {
        name: "Cancel",
      });

      expect(cancelButton).toBeEnabled();
    });

    it("shows a button to delete topic", () => {
      const dialog = screen.getByRole("dialog");
      const confirmButton = within(dialog).getByRole("button", {
        name: "Request topic deletion",
      });

      expect(confirmButton).toBeEnabled();
    });
  });

  describe("shows disabled buttons and checkbox when isLoading is true", () => {
    beforeAll(() => {
      render(
        <TopicDeleteConfirmationModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={true}
        />
      );
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("disables checkbox to delete associated schema as disabled", () => {
      const dialog = screen.getByRole("dialog");
      const checkbox = within(dialog).getByRole("checkbox", {
        name: "Delete all versions of schema associated with this topic if a schema exists.",
      });

      expect(checkbox).toBeDisabled();
      expect(checkbox).not.toBeChecked();
    });

    it("disables textarea where user can add a comment why they delete the topic", () => {
      const dialog = screen.getByRole("dialog");
      const textarea = within(dialog).getByRole("textbox", {
        name: "You can add the reason to delete the topic (optional)",
      });

      expect(textarea).toBeDisabled();
    });

    it("disables button to cancel process", () => {
      const dialog = screen.getByRole("dialog");
      const cancelButton = within(dialog).getByRole("button", {
        name: "Cancel",
      });

      expect(cancelButton).toBeDisabled();
    });

    it("disables button to delete topic and shows loading indicator", () => {
      const dialog = screen.getByRole("dialog");
      const confirmButton = within(dialog).getByRole("button", {
        name: "Request topic deletion",
      });
      const loadingAnimation =
        within(confirmButton).getByTestId("loading-button");

      expect(confirmButton).toBeDisabled();
      expect(loadingAnimation).toBeVisible();
    });
  });

  describe("enables user to cancel process", () => {
    beforeEach(() => {
      render(
        <TopicDeleteConfirmationModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers a given onClose function if user clicks cancel", async () => {
      const dialog = screen.getByRole("dialog");
      const cancelButton = within(dialog).getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  describe("enables user to start the deletion process", () => {
    beforeEach(() => {
      render(
        <TopicDeleteConfirmationModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers a given submit function with correct date when user does not check checkbox or adds a reason", async () => {
      const dialog = screen.getByRole("dialog");

      const confirmationButton = within(dialog).getByRole("button", {
        name: "Request topic deletion",
      });

      await user.click(confirmationButton);

      expect(mockOnSubmit).toHaveBeenCalledWith({
        remark: undefined,
        deleteAssociatedSchema: false,
      });
      expect(mockOnClose).not.toHaveBeenCalled();
    });

    it("triggers a given submit function with correct date when user does check checkbox and not adds a reason", async () => {
      const dialog = screen.getByRole("dialog");
      const checkbox = within(dialog).getByRole("checkbox", {
        name: "Delete all versions of schema associated with this topic if a schema exists.",
      });

      const confirmationButton = within(dialog).getByRole("button", {
        name: "Request topic deletion",
      });

      await user.click(checkbox);
      await user.click(confirmationButton);

      expect(mockOnSubmit).toHaveBeenCalledWith({
        remark: undefined,
        deleteAssociatedSchema: true,
      });
      expect(mockOnClose).not.toHaveBeenCalled();
    });

    it("triggers a given submit function with correct date when user does check checkbox and adds a reason", async () => {
      const dialog = screen.getByRole("dialog");
      const checkbox = within(dialog).getByRole("checkbox", {
        name: "Delete all versions of schema associated with this topic if a schema exists.",
      });
      const textarea = within(dialog).getByRole("textbox", {
        name: "You can add the reason to delete the topic (optional)",
      });

      const confirmationButton = within(dialog).getByRole("button", {
        name: "Request topic deletion",
      });

      await user.click(checkbox);
      await user.type(textarea, "This is my reason");
      await user.click(confirmationButton);

      expect(mockOnSubmit).toHaveBeenCalledWith({
        remark: "This is my reason",
        deleteAssociatedSchema: true,
      });
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });
});
