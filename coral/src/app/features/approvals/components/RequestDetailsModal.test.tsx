import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import RequestDetailsModal from "src/app/features/approvals/components/RequestDetailsModal";

const baseProps = {
  onClose: jest.fn(),
  onApprove: jest.fn(),
  onReject: jest.fn(),
};

describe("RequestDetailsModal.test", () => {
  afterEach(cleanup);

  it("renders a Modal with correct elements (isLoading false)", () => {
    render(
      <RequestDetailsModal {...baseProps} isLoading={false}>
        <div>content</div>
      </RequestDetailsModal>
    );

    expect(
      screen.getByRole("heading", { name: "Request details" })
    ).toBeVisible();
    expect(screen.getByText("content")).toBeVisible();
    expect(screen.getByRole("button", { name: "Close modal" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Close modal" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Approve" })).toBeEnabled();
    expect(screen.getByRole("button", { name: "Approve" })).toBeEnabled();
    expect(screen.getByRole("button", { name: "Reject" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Reject" })).toBeEnabled();
  });

  it("renders a Modal with correct elements (isLoading true)", () => {
    render(
      <RequestDetailsModal {...baseProps} isLoading={true}>
        <div>content</div>
      </RequestDetailsModal>
    );

    expect(
      screen.getByRole("heading", { name: "Request details" })
    ).toBeVisible();
    expect(screen.getByText("content")).toBeVisible();
    expect(screen.getByRole("button", { name: "Close modal" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Close modal" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Approve" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Approve" })).toBeDisabled();
    expect(screen.getByRole("button", { name: "Reject" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Reject" })).toBeDisabled();
  });

  it("calls correct functions when clicking buttons", async () => {
    render(
      <RequestDetailsModal {...baseProps} isLoading={false}>
        <div>content</div>
      </RequestDetailsModal>
    );

    const { onClose, onApprove, onReject } = baseProps;

    const closeButton = screen.getByRole("button", { name: "Close modal" });
    const approveButton = screen.getByRole("button", { name: "Approve" });
    const rejectButton = screen.getByRole("button", { name: "Reject" });

    await userEvent.click(closeButton);
    expect(onClose).toHaveBeenCalledTimes(1);

    await userEvent.click(approveButton);
    expect(onApprove).toHaveBeenCalledTimes(1);

    await userEvent.click(rejectButton);
    expect(onReject).toHaveBeenCalledTimes(1);
  });
});
