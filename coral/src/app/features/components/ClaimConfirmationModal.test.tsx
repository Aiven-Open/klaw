import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ClaimConfirmationModal } from "src/app/features/components/ClaimConfirmationModal";

const mockOnClose = jest.fn();
const mockOnSubmit = jest.fn();
describe("ClaimConfirmationModal", () => {
  const user = userEvent.setup();

  describe("renders all necessary elements (type topic)", () => {
    describe("renders all necessary elements when isLoading is false", () => {
      beforeAll(() => {
        render(
          <ClaimConfirmationModal
            onClose={mockOnClose}
            onSubmit={mockOnSubmit}
            isLoading={false}
            entity={"topic"}
          />
        );
      });

      afterAll(cleanup);

      it("shows a dialog element", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });

        expect(dialog).toBeVisible();
      });

      it("shows a headline", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });
        const headline = within(dialog).getByRole("heading", {
          name: "Claim topic",
        });

        expect(headline).toBeVisible();
      });

      it("shows more information to claim the topic", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });
        const text = within(dialog).getByText(
          "Are you sure you would like to claim this topic?"
        );

        expect(text).toBeVisible();
      });

      it("shows a textarea where user can add a comment why they claim the topic", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });
        const textarea = within(dialog).getByRole("textbox", {
          name: "You can add the reason to claim the topic (optional)",
        });

        expect(textarea).toBeEnabled();
      });

      it("shows a button to cancel process", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });
        const cancelButton = within(dialog).getByRole("button", {
          name: "Cancel",
        });

        expect(cancelButton).toBeEnabled();
      });

      it("shows a button to claim topic", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });
        const confirmButton = within(dialog).getByRole("button", {
          name: "Request claim topic",
        });

        expect(confirmButton).toBeEnabled();
      });
    });

    describe("shows disabled buttons when isLoading is true", () => {
      beforeAll(() => {
        render(
          <ClaimConfirmationModal
            onClose={mockOnClose}
            onSubmit={mockOnSubmit}
            isLoading={true}
            entity={"topic"}
          />
        );
      });

      afterAll(cleanup);

      it("disables textarea where user can add a comment why they claim the topic", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });
        const textarea = within(dialog).getByRole("textbox", {
          name: "You can add the reason to claim the topic (optional)",
        });

        expect(textarea).toBeDisabled();
      });

      it("disables button to cancel process", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });
        const cancelButton = within(dialog).getByRole("button", {
          name: "Cancel",
        });

        expect(cancelButton).toBeDisabled();
      });

      it("disables button to claim topic and shows loading indicator", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim topic" });
        const confirmButton = within(dialog).getByRole("button", {
          name: "Request claim topic",
        });
        const loadingAnimation =
          within(confirmButton).getByTestId("loading-button");

        expect(confirmButton).toBeDisabled();
        expect(loadingAnimation).toBeVisible();
      });
    });
  });

  describe("renders all necessary elements (type connector)", () => {
    describe("renders all necessary elements when isLoading is false", () => {
      beforeAll(() => {
        render(
          <ClaimConfirmationModal
            onClose={mockOnClose}
            onSubmit={mockOnSubmit}
            isLoading={false}
            entity={"connector"}
          />
        );
      });

      afterAll(cleanup);

      it("shows a dialog element", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });

        expect(dialog).toBeVisible();
      });

      it("shows a headline", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });
        const headline = within(dialog).getByRole("heading", {
          name: "Claim connector",
        });

        expect(headline).toBeVisible();
      });

      it("shows more information to claim the connector", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });
        const text = within(dialog).getByText(
          "Are you sure you would like to claim this connector?"
        );

        expect(text).toBeVisible();
      });

      it("shows a textarea where user can add a comment why they claim the connector", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });
        const textarea = within(dialog).getByRole("textbox", {
          name: "You can add the reason to claim the connector (optional)",
        });

        expect(textarea).toBeEnabled();
      });

      it("shows a button to cancel process", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });
        const cancelButton = within(dialog).getByRole("button", {
          name: "Cancel",
        });

        expect(cancelButton).toBeEnabled();
      });

      it("shows a button to claim connector", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });
        const confirmButton = within(dialog).getByRole("button", {
          name: "Request claim connector",
        });

        expect(confirmButton).toBeEnabled();
      });
    });

    describe("shows disabled buttons when isLoading is true", () => {
      beforeAll(() => {
        render(
          <ClaimConfirmationModal
            onClose={mockOnClose}
            onSubmit={mockOnSubmit}
            isLoading={true}
            entity={"connector"}
          />
        );
      });

      afterAll(cleanup);

      it("disables textarea where user can add a comment why they claim the connector", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });
        const textarea = within(dialog).getByRole("textbox", {
          name: "You can add the reason to claim the connector (optional)",
        });

        expect(textarea).toBeDisabled();
      });

      it("disables button to cancel process", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });
        const cancelButton = within(dialog).getByRole("button", {
          name: "Cancel",
        });

        expect(cancelButton).toBeDisabled();
      });

      it("disables button to claim connector and shows loading indicator", () => {
        const dialog = screen.getByRole("dialog", { name: "Claim connector" });
        const confirmButton = within(dialog).getByRole("button", {
          name: "Request claim connector",
        });
        const loadingAnimation =
          within(confirmButton).getByTestId("loading-button");

        expect(confirmButton).toBeDisabled();
        expect(loadingAnimation).toBeVisible();
      });
    });
  });

  describe("enables user to cancel process (example topic)", () => {
    beforeAll(() => {
      render(
        <ClaimConfirmationModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
          entity={"topic"}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers a given onClose function if user clicks cancel", async () => {
      const dialog = screen.getByRole("dialog", { name: "Claim topic" });
      const cancelButton = within(dialog).getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  describe("enables user to start the claiming process (example topic)", () => {
    beforeEach(() => {
      render(
        <ClaimConfirmationModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
          entity={"topic"}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers a given submit function with correct date when user adds a reason", async () => {
      const dialog = screen.getByRole("dialog", { name: "Claim topic" });
      const textarea = within(dialog).getByRole("textbox", {
        name: "You can add the reason to claim the topic (optional)",
      });

      const confirmationButton = within(dialog).getByRole("button", {
        name: "Request claim topic",
      });

      await user.type(textarea, "This is my reason");
      await user.click(confirmationButton);

      expect(mockOnSubmit).toHaveBeenCalledWith("This is my reason");
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });
});
