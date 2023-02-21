import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";

const baseProps = {
  onClose: jest.fn(),
  onApprove: jest.fn(),
  onReject: jest.fn(),
};

describe("RequestDetailsModal.test", () => {
  describe("renders a Modal with correct elements when isLoading is false (before interaction)", () => {
    beforeAll(() => {
      render(
        <RequestDetailsModal {...baseProps} isLoading={false}>
          <div>content</div>
        </RequestDetailsModal>
      );
    });
    afterAll(cleanup);

    it("renders a Modal", () => {
      expect(screen.getByRole("dialog")).toBeVisible();
    });

    it("renders correct heading", () => {
      expect(
        screen.getByRole("heading", { name: "Request details" })
      ).toBeVisible();
    });

    it("renders enabled Close button", () => {
      expect(screen.getByRole("button", { name: "Close modal" })).toBeEnabled();
    });

    it("renders enabled Approve button", () => {
      expect(screen.getByRole("button", { name: "Approve" })).toBeEnabled();
    });

    it("renders enabled Reject request button", () => {
      expect(screen.getByRole("button", { name: "Reject" })).toBeEnabled();
    });
  });

  describe("renders a Modal with correct elements when isLoading is true", () => {
    beforeAll(() => {
      render(
        <RequestDetailsModal {...baseProps} isLoading={true}>
          <div>content</div>
        </RequestDetailsModal>
      );
    });
    afterAll(cleanup);

    it("renders correct heading", () => {
      expect(
        screen.getByRole("heading", { name: "Request details" })
      ).toBeVisible();
    });

    it("renders disabled Close button", () => {
      expect(
        screen.getByRole("button", { name: "Close modal" })
      ).toBeDisabled();
    });

    it("renders disabled Approve button", () => {
      expect(screen.getByRole("button", { name: "Approve" })).toBeDisabled();
    });

    it("renders disabled Reject request button", () => {
      expect(screen.getByRole("button", { name: "Reject" })).toBeDisabled();
    });
  });

  describe("renders a Modal with correct elements when disabledActions is true", () => {
    beforeAll(() => {
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
    afterAll(cleanup);

    it("renders correct heading", () => {
      expect(
        screen.getByRole("heading", { name: "Request details" })
      ).toBeVisible();
    });

    it("renders enabled Close button", () => {
      expect(screen.getByRole("button", { name: "Close modal" })).toBeEnabled();
    });

    it("renders disabled Approve button", () => {
      expect(screen.getByRole("button", { name: "Approve" })).toBeDisabled();
    });

    it("renders disabled Reject request button", () => {
      expect(screen.getByRole("button", { name: "Reject" })).toBeDisabled();
    });
  });

  describe("handles user interaction", () => {
    beforeAll(() => {
      render(
        <RequestDetailsModal {...baseProps} isLoading={false}>
          <div>content</div>
        </RequestDetailsModal>
      );
    });
    afterAll(cleanup);

    it("user can close Modal", async () => {
      const { onClose } = baseProps;
      const closeButton = screen.getByRole("button", { name: "Close modal" });

      await userEvent.click(closeButton);
      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("user can approve", async () => {
      const { onApprove } = baseProps;
      const approveButton = screen.getByRole("button", { name: "Approve" });

      await userEvent.click(approveButton);
      expect(onApprove).toHaveBeenCalledTimes(1);
    });

    it("user can reject", async () => {
      const { onReject } = baseProps;

      const rejectButton = screen.getByRole("button", {
        name: "Reject",
      });

      await userEvent.click(rejectButton);
      expect(onReject).toHaveBeenCalledTimes(1);
    });
  });
});
