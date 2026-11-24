import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CancelRequestDialog } from "src/app/features/components/CancelRequestDialog";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("CancelRequestDialog.tsx", () => {
  describe("renders nothing when closed", () => {
    beforeAll(() => {
      customRender(
        <CancelRequestDialog
          open={false}
          title={"Cancel topic request?"}
          onConfirm={jest.fn()}
          onDismiss={jest.fn()}
        />,
        { aquariumContext: true }
      );
    });

    afterAll(() => {
      cleanup();
    });

    it("shows no dialog when open is false", () => {
      expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
    });
  });

  describe("renders the confirmation when open", () => {
    beforeAll(() => {
      customRender(
        <CancelRequestDialog
          open={true}
          title={"Cancel topic request?"}
          onConfirm={jest.fn()}
          onDismiss={jest.fn()}
        />,
        { aquariumContext: true }
      );
    });

    afterAll(() => {
      cleanup();
    });

    it("shows a warning dialog with the given title", () => {
      const dialog = screen.getByRole("alertdialog", {
        name: "Cancel topic request?",
      });

      expect(dialog).toBeVisible();
    });

    it("explains that the entered data will be lost", () => {
      const warning = screen.getByText(
        "Do you want to cancel this request? The data added will be lost."
      );

      expect(warning).toBeVisible();
    });

    it("shows a button to cancel the request", () => {
      const cancelButton = screen.getByRole("button", {
        name: "Cancel request",
      });
      expect(cancelButton).toBeEnabled();
    });

    it("shows a button to continue with the request", () => {
      const continueButton = screen.getByRole("button", {
        name: "Continue with request",
      });
      expect(continueButton).toBeEnabled();
    });
  });

  describe("enables user to confirm cancel or continue with request", () => {
    afterEach(() => {
      cleanup();
    });

    it('confirms the cancel when user clicks "Cancel request"', async () => {
      const onConfirm = jest.fn();
      customRender(
        <CancelRequestDialog
          open={true}
          title={"Cancel schema request?"}
          onConfirm={onConfirm}
          onDismiss={jest.fn()}
        />,
        { aquariumContext: true }
      );

      const cancelButton = screen.getByRole("button", {
        name: "Cancel request",
      });
      await userEvent.click(cancelButton);

      expect(onConfirm).toHaveBeenCalledTimes(1);
    });

    it('dismisses the dialog when user clicks "Continue with request"', async () => {
      const onDismiss = jest.fn();
      customRender(
        <CancelRequestDialog
          open={true}
          title={"Cancel ACL request?"}
          onConfirm={jest.fn()}
          onDismiss={onDismiss}
        />,
        { aquariumContext: true }
      );

      const continueButton = screen.getByRole("button", {
        name: "Continue with request",
      });
      await userEvent.click(continueButton);

      expect(onDismiss).toHaveBeenCalledTimes(1);
    });
  });
});
