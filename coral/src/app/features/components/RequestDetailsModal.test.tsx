import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import RequestDetailsModal from "src/app/features/components/RequestDetailsModal";

const primaryActionText = "Approve";
const primaryActionOnClick = vi.fn();
const secondaryActionText = "Decline";
const secondaryActionOnClick = vi.fn();

const baseProps = {
  onClose: vi.fn(),
  actions: {
    primary: {
      text: primaryActionText,
      onClick: primaryActionOnClick,
    },
    secondary: {
      text: secondaryActionText,
      onClick: secondaryActionOnClick,
    },
  },
};

describe("RequestDetailsModal.test", () => {
  const headlineText = "Request details";

  describe("renders a Modal with correct elements when isLoading is false (before interaction)", () => {
    beforeEach(() => {
      render(
        <RequestDetailsModal {...baseProps} isLoading={false}>
          <div>content</div>
        </RequestDetailsModal>
      );
    });
    afterEach(cleanup);

    it("renders a Modal", () => {
      expect(screen.getByRole("dialog")).toBeVisible();
    });

    it("renders correct heading", () => {
      expect(screen.getByRole("heading", { name: headlineText })).toBeVisible();
    });

    it("renders enabled Close button", () => {
      expect(screen.getByRole("button", { name: "Close modal" })).toBeEnabled();
    });

    it("renders enabled Approve button", () => {
      expect(
        screen.getByRole("button", { name: primaryActionText })
      ).toBeEnabled();
    });

    it("renders enabled Decline request button", () => {
      expect(
        screen.getByRole("button", { name: secondaryActionText })
      ).toBeEnabled();
    });
  });

  describe("renders a Modal with correct elements when isLoading is true", () => {
    beforeEach(() => {
      render(
        <RequestDetailsModal {...baseProps} isLoading={true}>
          <div>content</div>
        </RequestDetailsModal>
      );
    });
    afterEach(cleanup);

    it("renders correct heading", () => {
      expect(screen.getByRole("heading", { name: headlineText })).toBeVisible();
    });

    it("renders disabled Close button", () => {
      expect(
        screen.getByRole("button", { name: "Close modal" })
      ).toBeDisabled();
    });

    it("renders disabled Approve button", () => {
      expect(
        screen.getByRole("button", { name: primaryActionText })
      ).toBeDisabled();
    });

    it("renders disabled Decline request button", () => {
      expect(
        screen.getByRole("button", { name: secondaryActionText })
      ).toBeDisabled();
    });
  });

  describe("renders a Modal with correct elements when disabledActions is true", () => {
    beforeEach(() => {
      render(
        <RequestDetailsModal
          {...baseProps}
          isLoading={false}
          disabledActions={true}
        >
          <div>content</div>
        </RequestDetailsModal>
      );
    });
    afterEach(cleanup);

    it("renders correct heading", () => {
      expect(screen.getByRole("heading", { name: headlineText })).toBeVisible();
    });

    it("renders enabled Close button", () => {
      expect(screen.getByRole("button", { name: "Close modal" })).toBeEnabled();
    });

    it("renders disabled Approve button", () => {
      expect(
        screen.getByRole("button", { name: primaryActionText })
      ).toBeDisabled();
    });

    it("renders disabled Decline request button", () => {
      expect(
        screen.getByRole("button", { name: secondaryActionText })
      ).toBeDisabled();
    });
  });

  describe("renders a Modal with correct elements when secondary is disabled", () => {
    beforeEach(() => {
      const props = {
        ...baseProps,
        actions: {
          ...baseProps.actions,
          secondary: {
            text: secondaryActionText,
            onClick: secondaryActionOnClick,
            disabled: true,
          },
        },
      };
      render(
        <RequestDetailsModal {...props} isLoading={false}>
          <div>content</div>
        </RequestDetailsModal>
      );
    });
    afterEach(cleanup);

    it("renders correct heading", () => {
      expect(screen.getByRole("heading", { name: headlineText })).toBeVisible();
    });

    it("renders enabled Close button", () => {
      expect(screen.getByRole("button", { name: "Close modal" })).toBeEnabled();
    });

    it("renders disabled Approve button", () => {
      expect(
        screen.getByRole("button", { name: primaryActionText })
      ).toBeEnabled();
    });

    it("renders disabled Decline request button", () => {
      expect(
        screen.getByRole("button", { name: secondaryActionText })
      ).toBeDisabled();
    });
  });

  describe("handles user interaction", () => {
    beforeEach(() => {
      render(
        <RequestDetailsModal {...baseProps} isLoading={false}>
          <div>content</div>
        </RequestDetailsModal>
      );
    });
    afterEach(cleanup);

    it("user can close Modal", async () => {
      const { onClose } = baseProps;
      const closeButton = screen.getByRole("button", { name: "Close modal" });

      await userEvent.click(closeButton);
      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("triggers the primary action when user clicks the primary button", async () => {
      const approveButton = screen.getByRole("button", {
        name: primaryActionText,
      });

      await userEvent.click(approveButton);
      expect(primaryActionOnClick).toHaveBeenCalledTimes(1);
    });

    it("triggers the secondary action when user clicks the secondary button", async () => {
      const declineButton = screen.getByRole("button", {
        name: secondaryActionText,
      });

      await userEvent.click(declineButton);
      expect(secondaryActionOnClick).toHaveBeenCalledTimes(1);
    });
  });
});
