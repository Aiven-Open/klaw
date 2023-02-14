import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import RequestRejectModal from "src/app/features/approvals/components/RequestRejectModal";

const baseProps = {
  onClose: jest.fn(),
  onSubmit: jest.fn(),
  onCancel: jest.fn(),
};

describe("RequestRejectModal.test", () => {
  afterEach(cleanup);

  it("renders a Modal with correct elements on first load (isLoading false)", () => {
    render(<RequestRejectModal {...baseProps} isLoading={false} />);

    expect(
      screen.getByRole("heading", { name: "Reject request" })
    ).toBeVisible();
    expect(
      screen.getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      })
    ).toBeVisible();
    expect(
      screen.getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      })
    ).toBeEnabled();
    expect(screen.getByRole("button", { name: "Close modal" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Close modal" })).toBeEnabled();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeEnabled();
    expect(
      screen.getByRole("button", { name: "Reject request" })
    ).toBeVisible();
    expect(
      screen.getByRole("button", { name: "Reject request" })
    ).toBeDisabled();
  });

  it("renders a Modal with correct elements (isLoading true)", () => {
    render(<RequestRejectModal {...baseProps} isLoading={true} />);

    expect(
      screen.getByRole("heading", { name: "Reject request" })
    ).toBeVisible();
    expect(
      screen.getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      })
    ).toBeVisible();
    expect(
      screen.getByRole("textbox", {
        name: "Submit a reason to decline the request *",
      })
    ).toBeDisabled();
    expect(screen.getByRole("button", { name: "Close modal" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Close modal" })).toBeEnabled();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeDisabled();
    expect(
      screen.getByRole("button", { name: "Reject request" })
    ).toBeVisible();
    expect(
      screen.getByRole("button", { name: "Reject request" })
    ).toBeDisabled();
  });

  it("calls correct functions when clicking close and cancel buttons", async () => {
    render(<RequestRejectModal {...baseProps} isLoading={false} />);

    const { onClose, onCancel } = baseProps;

    const closeButton = screen.getByRole("button", { name: "Close modal" });
    const cancelButton = screen.getByRole("button", { name: "Cancel" });

    await userEvent.click(closeButton);
    expect(onClose).toHaveBeenCalledTimes(1);

    await userEvent.click(cancelButton);
    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it("calls correct function when clicking reject request button", async () => {
    render(<RequestRejectModal {...baseProps} isLoading={false} />);

    const { onSubmit } = baseProps;

    const rejectButton = screen.getByRole("button", { name: "Reject request" });
    const textArea = screen.getByRole("textbox", {
      name: "Submit a reason to decline the request *",
    });

    await userEvent.type(textArea, "reason");
    await userEvent.click(rejectButton);
    expect(onSubmit).toHaveBeenCalledWith("reason");
  });

  it("shows error when entering a too long message", async () => {
    render(<RequestRejectModal {...baseProps} isLoading={false} />);
    const tooLong =
      "Quisque commodo aliquam tristique. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Sed ornare turpis ac cursus vulputate. Morbi auctor sodales porttitor. Mauris placerat ante id facilisis vehicula. Pellentesque ornare quis massa elementum auctor. Suspendisse potenti. Phasellus dignissim sit amet risus vitae aliquet. Vivamus at dolor vehicula, placerat odio sit amet, imperdiet enim. Donec scelerisque pretium metus ut dignissim. Morbi posuere tortor in cursus porttitor. Maecenas a diam ut urna mattis convallis a vel ligula.";

    const rejectButton = screen.getByRole("button", { name: "Reject request" });
    const textArea = screen.getByRole("textbox", {
      name: "Submit a reason to decline the request *",
    });

    await userEvent.type(textArea, tooLong);
    expect(rejectButton).toBeDisabled();
    expect(textArea).toBeInvalid();
    expect(screen.getByText("Rejection message is too long.")).toBeVisible();
  });
});
