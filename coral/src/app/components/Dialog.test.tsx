import { cleanup, render, screen, within } from "@testing-library/react";
import { Dialog } from "src/app/components/Dialog";
import confirm from "@aivenio/aquarium/dist/src/icons/confirm";
import warningSign from "@aivenio/aquarium/dist/src/icons/warningSign";
import error from "@aivenio/aquarium/dist/src/icons/error";

describe("Dialog.tsx", () => {
  const testTitle = "Dialog title";
  const mockChildren = <div>This is child content</div>;
  const mockPrimary = {
    text: "Primary action 1",
    onClick: jest.fn(),
  };
  const mockSecondary = {
    text: "Secondary action 1",
    onClick: jest.fn(),
  };

  afterEach(jest.clearAllMocks);

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      render(
        <>
          <div id={"root"}></div>
          <Dialog
            type="confirmation"
            title={testTitle}
            primaryAction={mockPrimary}
            secondaryAction={mockSecondary}
          >
            {mockChildren}
          </Dialog>
        </>
      );
    });

    afterAll(cleanup);

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
      const text = screen.getByText("This is child content");

      expect(text).toBeVisible();
    });

    it("shows a button with a given primary action", () => {
      const button = screen.getByRole("button", { name: mockPrimary.text });

      expect(button).toBeEnabled();
    });

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

  describe("renders header icon and color dependent on type prop", () => {
    afterEach(cleanup);

    it("shows a confirmation header", () => {
      render(
        <>
          <div id={"root"}></div>
          <Dialog
            type="confirmation"
            title={testTitle}
            primaryAction={mockPrimary}
            secondaryAction={mockSecondary}
          >
            {mockChildren}
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
          <Dialog
            type="warning"
            title={testTitle}
            primaryAction={mockPrimary}
            secondaryAction={mockSecondary}
          >
            {mockChildren}
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
          <Dialog
            type="danger"
            title={testTitle}
            primaryAction={mockPrimary}
            secondaryAction={mockSecondary}
          >
            {mockChildren}
          </Dialog>
        </>
      );

      const headline = screen.getByRole("heading");
      const icon = within(headline).getByTestId("ds-icon");

      expect(headline).toHaveClass("text-error-70");
      expect(icon).toHaveAttribute("data-icon", error.body);
    });
  });
});
