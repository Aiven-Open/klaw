import { cleanup, render, RenderResult, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Modal } from "src/app/components/Modal";

describe("Modal.tsx", () => {
  const testTitle = "Modal title";
  const mockChildren = <div>This is child content</div>;
  const mockPrimary = {
    text: "Primary action 1",
    onClick: jest.fn(),
  };

  describe("renders a default modal with all necessary elements", () => {
    afterEach(jest.clearAllMocks);

    describe("renders all necessary elements", () => {
      beforeAll(() => {
        render(
          <>
            <div id={"root"}></div>
            <Modal title={testTitle} primaryAction={mockPrimary}>
              {mockChildren}
            </Modal>
          </>
        );
      });

      afterAll(cleanup);

      it("shows a dialog", () => {
        const dialog = screen.getByRole("dialog");

        expect(dialog).toBeVisible();
      });

      it("marks the modal as a aria modal to help assistive technology", () => {
        const dialog = screen.getByRole("dialog");

        expect(dialog).toHaveAttribute("aria-modal", "true");
      });

      it("shows a given title", () => {
        const title = screen.getByRole("heading", { name: testTitle });

        expect(title).toBeVisible();
      });

      it("moves focus to the title in the dialog", () => {
        const title = screen.getByRole("heading", { name: testTitle });

        expect(title).toHaveFocus();
      });

      it("shows the given content", () => {
        const text = screen.getByText("This is child content");

        expect(text).toBeVisible();
      });

      it("shows a button with a given primary action", () => {
        const button = screen.getByRole("button", { name: mockPrimary.text });

        expect(button).toBeEnabled();
      });

      it("shows no other buttons for secondary action or close", () => {
        const buttons = screen.getAllByRole("button");

        expect(buttons).toHaveLength(1);
        expect(buttons[0]).toHaveTextContent(mockPrimary.text);
      });
    });

    describe("handles user input", () => {
      beforeEach(() => {
        render(
          <>
            <div id={"root"}></div>
            <Modal title={testTitle} primaryAction={mockPrimary}>
              {mockChildren}
            </Modal>
          </>
        );
      });

      afterEach(cleanup);

      it("enables user to click the secondary action", async () => {
        const button = screen.getByRole("button", { name: mockPrimary.text });

        await userEvent.click(button);
        expect(mockPrimary.onClick).toHaveBeenCalled();
      });

      it("enables user access and trigger the primary action with keyboard", async () => {
        await userEvent.tab();
        const button = screen.getByRole("button", { name: mockPrimary.text });

        expect(button).toHaveFocus();

        await userEvent.keyboard("{Enter}");
        expect(mockPrimary.onClick).toHaveBeenCalled();
      });
    });

    describe("traps user input inside the modal", () => {
      let component: RenderResult;

      beforeEach(() => {
        component = render(
          <>
            <div id={"root"}></div>
            <Modal title={testTitle} primaryAction={mockPrimary}>
              {mockChildren}
            </Modal>
          </>
        );
      });

      afterEach(cleanup);

      it("sets the aria hidden value on app root", () => {
        const root = document.getElementById("root");
        expect(root).toHaveAttribute("aria-hidden", "true");

        component.unmount();
        expect(root).not.toHaveAttribute("aria-hidden");
      });

      it("sets the tabindex of app root", () => {
        const root = document.getElementById("root");
        expect(root).toHaveAttribute("tabindex", "-1");

        component.unmount();
        expect(root).not.toHaveAttribute("tabindex");
      });

      it("sets the attribute inert for app root", () => {
        const root = document.getElementById("root");
        expect(root).toHaveAttribute("inert", "true");

        component.unmount();
        expect(root).not.toHaveAttribute("inert");
      });
    });
  });

  describe("renders additional elements dependent on props", () => {
    describe("handles a secondary action", () => {
      const mockSecondary = {
        text: "this is the secondary action",
        onClick: jest.fn(),
      };

      beforeEach(() => {
        render(
          <Modal
            title={testTitle}
            primaryAction={mockPrimary}
            secondaryAction={mockSecondary}
          >
            {mockChildren}
          </Modal>
        );
      });

      afterEach(cleanup);

      it("shows the secondary action as a button", () => {
        const button = screen.getByRole("button", { name: mockSecondary.text });

        expect(button).toBeEnabled();
      });

      it("shows two buttons when secodary and primary action are given", () => {
        const buttons = screen.getAllByRole("button");

        expect(buttons).toHaveLength(2);
      });

      it("enables user to click the secondary action", async () => {
        const button = screen.getByRole("button", { name: mockSecondary.text });

        await userEvent.click(button);
        expect(mockSecondary.onClick).toHaveBeenCalled();
      });

      it("enables user access and trigger the secondary action with keyboard", async () => {
        const buttonPrimary = screen.getByRole("button", {
          name: mockPrimary.text,
        });
        const buttonSecondary = screen.getByRole("button", {
          name: mockSecondary.text,
        });
        await userEvent.tab();
        expect(buttonSecondary).toHaveFocus();
        await userEvent.tab();
        expect(buttonPrimary).toHaveFocus();

        await userEvent.tab({ shift: true });
        expect(buttonSecondary).toHaveFocus();

        await userEvent.keyboard("{Enter}");
        expect(mockSecondary.onClick).toHaveBeenCalled();
      });
    });

    describe("adds option to close the modal without triggering an action", () => {
      const mockClose = jest.fn();

      beforeEach(() => {
        render(
          <Modal
            title={testTitle}
            primaryAction={mockPrimary}
            close={mockClose}
          >
            {mockChildren}
          </Modal>
        );
      });

      afterEach(cleanup);

      it("shows a close button", () => {
        const button = screen.getByRole("button", { name: "Close modal" });

        expect(button).toBeEnabled();
      });

      it("enables user to click close the button with the mouse", async () => {
        const button = screen.getByRole("button", { name: "Close modal" });

        await userEvent.click(button);
        expect(mockClose).toHaveBeenCalled();
      });

      it("enables user access and trigger close with the keyboard", async () => {
        const closeButton = screen.getByRole("button", { name: "Close modal" });
        await userEvent.tab();
        expect(closeButton).toHaveFocus();

        await userEvent.click(closeButton);
        expect(mockClose).toHaveBeenCalled();
      });

      it("enables user to close the modal with pressing the Escape key", async () => {
        await userEvent.keyboard("{Escape}");

        expect(mockClose).toHaveBeenCalled();
      });
    });

    describe("handles Modal actions isLoading state", () => {
      const mockClose = jest.fn();
      const mockSecondary = {
        text: "this is the secondary action",
        onClick: jest.fn(),
        loading: true,
      };

      beforeEach(() => {
        render(
          <Modal
            title={testTitle}
            primaryAction={{ ...mockPrimary, loading: true }}
            secondaryAction={mockSecondary}
            close={mockClose}
          >
            {mockChildren}
          </Modal>
        );
      });

      afterEach(cleanup);

      it("disables primary and secondary action button", () => {
        const buttonPrimary = screen.getByRole("button", {
          name: mockPrimary.text,
        });
        const buttonSecondary = screen.getByRole("button", {
          name: mockSecondary.text,
        });

        expect(buttonPrimary).toBeDisabled();
        expect(buttonSecondary).toBeDisabled();
      });
    });

    describe("handles Modal actions disabled state", () => {
      const mockClose = jest.fn();
      const mockSecondary = {
        text: "this is the secondary action",
        onClick: jest.fn(),
        disabled: true,
      };

      beforeEach(() => {
        render(
          <Modal
            title={testTitle}
            primaryAction={{ ...mockPrimary, disabled: true }}
            secondaryAction={mockSecondary}
            close={mockClose}
          >
            {mockChildren}
          </Modal>
        );
      });

      afterEach(cleanup);

      it("disables primary and secondary action button", () => {
        const buttonPrimary = screen.getByRole("button", {
          name: mockPrimary.text,
        });
        const buttonSecondary = screen.getByRole("button", {
          name: mockSecondary.text,
        });

        expect(buttonPrimary).toBeDisabled();
        expect(buttonSecondary).toBeDisabled();
      });
    });
  });
});
