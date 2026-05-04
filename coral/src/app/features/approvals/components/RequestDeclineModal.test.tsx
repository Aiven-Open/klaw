import { cleanup, render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import RequestDeclineModal from "src/app/features/approvals/components/RequestDeclineModal";

const baseProps = {
  onClose: jest.fn(),
  onSubmit: jest.fn(),
  onCancel: jest.fn(),
};

describe("RequestDeclineModal.test", () => {
  describe("renders a Modal with correct elements when isLoading is false (before interaction)", () => {
    beforeAll(() => {
      render(<RequestDeclineModal {...baseProps} isLoading={false} />);
    });
    afterAll(cleanup);

    it("renders a Modal", () => {
      expect(screen.getByRole("dialog")).toBeVisible();
    });

    it("renders correct heading", () => {
      expect(
        screen.getByRole("heading", { name: "Decline request" })
      ).toBeVisible();
    });

    it("renders enabled Reason too decline field", () => {
      expect(
        screen.getByRole("textbox", {
          name: "Submit a reason to decline the request *",
        })
      ).toBeEnabled();
    });

    it("renders enabled Close button", () => {
      expect(screen.getByRole("button", { name: "Close modal" })).toBeEnabled();
    });

    it("renders enabled Cancel button", () => {
      expect(screen.getByRole("button", { name: "Cancel" })).toBeEnabled();
    });

    it("renders disabled Decline request button", () => {
      expect(
        screen.getByRole("button", { name: "Decline request" })
      ).toBeDisabled();
    });
  });

  describe("renders a Modal with correct elements when isLoading is true", () => {
    beforeAll(() => {
      render(<RequestDeclineModal {...baseProps} isLoading={true} />);
    });
    afterAll(cleanup);

    it("renders correct heading", () => {
      expect(
        screen.getByRole("heading", { name: "Decline request" })
      ).toBeVisible();
    });

    it("renders disabled Reason too decline field", () => {
      expect(
        screen.getByRole("textbox", {
          name: "Submit a reason to decline the request *",
        })
      ).toBeDisabled();
    });

    it("renders enabled Close button", () => {
      expect(
        screen.getByRole("button", { name: "Close modal" })
      ).toBeDisabled();
    });

    it("renders disabled Cancel button", () => {
      expect(screen.getByRole("button", { name: "Cancel" })).toBeDisabled();
    });

    it("renders disabled Decline request button", () => {
      expect(
        screen.getByRole("button", { name: "Decline request" })
      ).toBeDisabled();
    });
  });

  describe("handles user interaction", () => {
    beforeAll(() => {
      render(<RequestDeclineModal {...baseProps} isLoading={false} />);
    });
    afterAll(cleanup);

    it("user can close Modal", async () => {
      const { onClose } = baseProps;
      const closeButton = screen.getByRole("button", { name: "Close modal" });

      await userEvent.click(closeButton);
      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("user can cancel", async () => {
      const { onCancel } = baseProps;

      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      await userEvent.click(cancelButton);
      expect(onCancel).toHaveBeenCalledTimes(1);
    });

    it("user can decline", async () => {
      const { onSubmit } = baseProps;

      const declineButton = screen.getByRole("button", {
        name: "Decline request",
      });
      const textArea = screen.getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });

      await userEvent.type(textArea, "reason");
      await userEvent.click(declineButton);
      expect(onSubmit).toHaveBeenCalledWith("reason");
    });

    it("shows error when entering a too long message", async () => {
      const tooLong = "x".repeat(301);

      const declineButton = screen.getByRole("button", {
        name: "Decline request",
      });
      const textArea = screen.getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      });

      await userEvent.type(textArea, tooLong, { delay: 0 });
      expect(declineButton).toBeDisabled();
      expect(textArea).toBeInvalid();
    });
  });
});
