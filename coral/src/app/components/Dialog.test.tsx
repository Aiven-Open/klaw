import { cleanup, render, screen, within } from "@testing-library/react";
import { Dialog } from "src/app/components/Dialog";
import confirm from "@aivenio/aquarium/dist/src/icons/confirm";
import warningSign from "@aivenio/aquarium/dist/src/icons/warningSign";
import error from "@aivenio/aquarium/dist/src/icons/error";
import userEvent from "@testing-library/user-event";

describe("Dialog.tsx", () => {
  const testTitle = "Dialog title";
  const textContent = "This is the content given!";
  const mockPrimary = {
    text: "Primary action 1",
    onClick: vi.fn(),
  };

  afterEach(vi.clearAllMocks);

  describe("shows a default modal with headline, text and one button", () => {
    beforeEach(() => {
      render(
        <>
          <div id={"root"}></div>
          <Dialog
            type="confirmation"
            title={testTitle}
            primaryAction={mockPrimary}
          >
            {textContent}
          </Dialog>
        </>
      );
    });

    afterEach(cleanup);

    it("shows a dialog", () => {
      const dialog = screen.getByRole("dialog");

      expect(dialog).toBeVisible();
      expect(dialog).toHaveAttribute("aria-modal", "true");
    });

    it("shows a given title with an icon", () => {
      const title = screen.getByRole("heading", { name: testTitle });

      expect(title).toBeVisible();
    });

    it("shows the given content", () => {
      const text = screen.getByText(textContent);

      expect(text).toBeVisible();
    });

    it("shows a button with a given primary action", () => {
      const button = screen.getByRole("button", { name: mockPrimary.text });

      expect(button).toBeEnabled();
    });
  });

  describe("shows a second button dependent on a prop", () => {
    const mockSecondary = {
      text: "Secondary action 1",
      onClick: vi.fn(),
    };

    beforeEach(() => {
      render(
        <>
          <div id={"root"}></div>
          <Dialog
            type="confirmation"
            title={testTitle}
            primaryAction={mockPrimary}
            secondaryAction={mockSecondary}
          >
            {textContent}
          </Dialog>
        </>
      );
    });

    afterEach(cleanup);

    it("shows a button with a given secondary action", () => {
      const button = screen.getByRole("button", { name: mockPrimary.text });

      expect(button).toBeEnabled();
    });

    it("shows no close button", () => {
      const buttons = screen.getAllByRole("button");

      expect(buttons).toHaveLength(2);
      expect(buttons[0]).toHaveTextContent(mockSecondary.text);
      expect(buttons[1]).toHaveTextContent(mockPrimary.text);
    });
  });

  describe("shows different header icons and colors dependent on type prop", () => {
    afterAll(cleanup);

    it("shows a confirmation header", () => {
      render(
        <>
          <div id={"root"}></div>
          <Dialog
            type="confirmation"
            title={testTitle}
            primaryAction={mockPrimary}
          >
            {textContent}
          </Dialog>
        </>
      );

      const headline = screen.getByRole("heading");
      const icon = within(headline).getByTestId("ds-icon");

      expect(headline).toHaveClass("text-info-70");
      expect(icon).toHaveAttribute("data-icon", confirm.body);
    });

    it("shows a warning header", () => {
      render(
        <>
          <div id={"root"}></div>
          <Dialog type="warning" title={testTitle} primaryAction={mockPrimary}>
            {textContent}
          </Dialog>
        </>
      );

      const headline = screen.getByRole("heading");
      const icon = within(headline).getByTestId("ds-icon");

      expect(headline).toHaveClass("text-secondary-70");
      expect(icon).toHaveAttribute("data-icon", warningSign.body);
    });

    it("shows a danger header", () => {
      render(
        <>
          <div id={"root"}></div>
          <Dialog type="danger" title={testTitle} primaryAction={mockPrimary}>
            {textContent}
          </Dialog>
        </>
      );

      const headline = screen.getByRole("heading");
      const icon = within(headline).getByTestId("ds-icon");

      expect(headline).toHaveClass("text-error-70");
      expect(icon).toHaveAttribute("data-icon", error.body);
    });
  });

  describe("enables user to use buttons", () => {
    const mockSecondary = {
      text: "Secondary action 1",
      onClick: vi.fn(),
    };

    beforeEach(() => {
      render(
        <>
          <div id={"root"}></div>
          <Dialog
            type="confirmation"
            title={testTitle}
            primaryAction={mockPrimary}
            secondaryAction={mockSecondary}
          >
            {textContent}
          </Dialog>
        </>
      );
    });

    afterEach(() => {
      vi.resetAllMocks();
      cleanup();
    });

    it("user can click the button for the primary action", async () => {
      const button = screen.getByRole("button", { name: mockPrimary.text });

      await userEvent.click(button);

      expect(mockPrimary.onClick).toHaveBeenCalled();
    });

    it("user can click the button for the secondary action", async () => {
      const button = screen.getByRole("button", { name: mockSecondary.text });

      await userEvent.click(button);

      expect(mockSecondary.onClick).toHaveBeenCalled();
    });
  });
});
