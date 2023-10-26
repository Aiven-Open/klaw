import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { SchemaPromotionModal } from "src/app/features/topics/details/schema/components/SchemaPromotionModal";

const mockOnSubmit = jest.fn();
const mockOnClose = jest.fn();
const testTargetEnv = "TST";
const testVersion = 1;

describe("SchemaPromotionModal", () => {
  const user = userEvent.setup();

  describe("renders all necessary elements for default promotion", () => {
    beforeAll(() => {
      render(
        <SchemaPromotionModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
          version={testVersion}
          targetEnvironment={testTargetEnv}
          showForceRegister={false}
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

    it("shows no warning or option to force register", () => {
      const warning = screen.queryByRole("alert");
      const checkbox = screen.queryByRole("checkbox");

      expect(warning).not.toBeInTheDocument();
      expect(checkbox).not.toBeInTheDocument();
    });

    it("shows more information to delete the topic", () => {
      const dialog = screen.getByRole("dialog");
      const headline = within(dialog).getByRole("heading", {
        name: `Promote schema to ${testTargetEnv}`,
      });
      const text = within(dialog).getByText(
        `Promote the Version ${testVersion} of the schema to ${testTargetEnv}?`
      );

      expect(headline).toBeVisible();
      expect(text).toBeVisible();
    });

    it("does not show a switch to  force register", () => {
      const forceRegisterSwitch = screen.queryByRole("checkbox", {
        name: "Force register Overrides standard validation processes of the schema registry.",
      });
      expect(forceRegisterSwitch).not.toBeInTheDocument();
    });

    it("shows a textarea where user can add a comment why they delete the topic", () => {
      const dialog = screen.getByRole("dialog");
      const textarea = within(dialog).getByRole("textbox", {
        name: "You can add the reason to promote the schema (optional)",
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
        name: "Request schema promotion",
      });

      expect(confirmButton).toBeEnabled();
    });
  });

  describe("shows disabled UI while loading is true for default promotion", () => {
    beforeAll(() => {
      render(
        <SchemaPromotionModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={true}
          version={testVersion}
          targetEnvironment={testTargetEnv}
          showForceRegister={false}
        />
      );
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("disables textarea where user can add a comment why they promote the schema", () => {
      const dialog = screen.getByRole("dialog");
      const textarea = within(dialog).getByRole("textbox", {
        name: "You can add the reason to promote the schema (optional)",
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
        name: "Request schema promotion",
      });
      const loadingAnimation =
        within(confirmButton).getByTestId("loading-button");

      expect(confirmButton).toBeDisabled();
      expect(loadingAnimation).toBeVisible();
    });
  });

  describe("enables user to cancel process for default promotion", () => {
    beforeEach(() => {
      render(
        <SchemaPromotionModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
          version={testVersion}
          targetEnvironment={testTargetEnv}
          showForceRegister={false}
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

  describe("enables user to start the promotion process", () => {
    beforeEach(() => {
      render(
        <SchemaPromotionModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
          version={testVersion}
          targetEnvironment={testTargetEnv}
          showForceRegister={false}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("triggers a given submit function with correct payload when user does not adds a reason", async () => {
      const dialog = screen.getByRole("dialog");

      const confirmationButton = within(dialog).getByRole("button", {
        name: "Request schema promotion",
      });

      await user.click(confirmationButton);

      expect(mockOnSubmit).toHaveBeenCalledWith({
        forceRegister: false,
        remarks: "",
      });
      expect(mockOnClose).not.toHaveBeenCalled();
    });

    it("triggers a given submit function with correct date when adds a reason", async () => {
      const dialog = screen.getByRole("dialog");

      const textarea = within(dialog).getByRole("textbox", {
        name: "You can add the reason to promote the schema (optional)",
      });

      const confirmationButton = within(dialog).getByRole("button", {
        name: "Request schema promotion",
      });

      await user.type(textarea, "This is my reason");
      await user.click(confirmationButton);

      expect(mockOnSubmit).toHaveBeenCalledWith({
        forceRegister: false,
        remarks: "This is my reason",
      });
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  describe("optional enables user to start the promotion process (showForceRegister={true})", () => {
    beforeEach(() => {
      render(
        <SchemaPromotionModal
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isLoading={false}
          version={testVersion}
          targetEnvironment={testTargetEnv}
          showForceRegister={true}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a warning about force register", async () => {
      const warning = screen.getByRole("alert");

      expect(warning).toBeVisible();
      expect(warning).toHaveTextContent("Uploaded schema appears invalid.");
    });

    it("shows a checkbox to confirm force register", async () => {
      const dialog = screen.getByRole("dialog");

      const forceRegisterSwitch = within(dialog).getByRole("checkbox");
      expect(forceRegisterSwitch).toHaveAccessibleName(
        /Force register schema promotion Warning: This will override standard validation process of the schema registry. Learn more/
      );
      expect(forceRegisterSwitch).toBeEnabled();
    });

    it("changes the submit button text and disables button until checkbox is checked", async () => {
      const dialog = screen.getByRole("dialog");

      const confirmationButton = within(dialog).getByRole("button", {
        name: "Force register",
      });
      const forceRegisterSwitch = within(dialog).getByRole("checkbox");

      expect(confirmationButton).toBeDisabled();

      await user.click(forceRegisterSwitch);

      expect(confirmationButton).toBeEnabled();
    });

    it("triggers a given submit function with correct data", async () => {
      const dialog = screen.getByRole("dialog");

      const forceRegisterSwitch = within(dialog).getByRole("checkbox");

      const textarea = within(dialog).getByRole("textbox", {
        name: "You can add the reason to promote the schema (optional)",
      });

      const confirmationButton = within(dialog).getByRole("button", {
        name: "Force register",
      });

      await user.click(forceRegisterSwitch);
      await user.type(textarea, "This is my reason");
      await user.click(confirmationButton);

      expect(mockOnSubmit).toHaveBeenCalledWith({
        forceRegister: true,
        remarks: "This is my reason",
      });
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });
});
