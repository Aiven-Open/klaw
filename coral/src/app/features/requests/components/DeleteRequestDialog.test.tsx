import { cleanup, render, screen, within } from "@testing-library/react";
import { DeleteRequestDialog } from "src/app/features/requests/components/DeleteRequestDialog";
import userEvent from "@testing-library/user-event";

describe("DeleteRequestDialog", () => {
  const cancelMock = jest.fn();
  const deleteRequestMock = jest.fn();

  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(
        <DeleteRequestDialog
          cancel={cancelMock}
          deleteRequest={deleteRequestMock}
        />
      );
    });

    afterAll(cleanup);

    it("shows an dialog", () => {
      const dialog = screen.getByRole("dialog");

      expect(dialog).toBeVisible();
    });

    it("shows a headline for the dialog", () => {
      const dialog = screen.getByRole("dialog");
      const headline = within(dialog).getByRole("heading", {
        name: "Delete request",
      });

      expect(headline).toBeVisible();
    });

    it("shows a text for the dialog", () => {
      const dialog = screen.getByRole("dialog");
      const textInfo = within(dialog).getByText(
        "Are you sure you want to delete the request?"
      );

      expect(textInfo).toBeVisible();
    });

    it("shows a button to cancel the deletion of the request", () => {
      const dialog = screen.getByRole("dialog");
      const button = within(dialog).getByRole("button", { name: "Cancel" });

      expect(button).toBeEnabled();
    });

    it("shows a button to confirm deletion of the request", () => {
      const dialog = screen.getByRole("dialog");
      const button = within(dialog).getByRole("button", {
        name: "Delete request",
      });

      expect(button).toBeEnabled();
    });
  });

  describe("handles user interaction", () => {
    beforeEach(() => {
      render(
        <DeleteRequestDialog
          cancel={cancelMock}
          deleteRequest={deleteRequestMock}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("calls the given cancel function when user clicks button to cancel the deletion of the request", async () => {
      const dialog = screen.getByRole("dialog");
      const button = within(dialog).getByRole("button", { name: "Cancel" });

      await userEvent.click(button);
      expect(cancelMock).toHaveBeenCalledTimes(1);
    });

    it("calls the given deleteRequest function when user clicks button to confirm deletion of the request", async () => {
      const dialog = screen.getByRole("dialog");
      const button = within(dialog).getByRole("button", {
        name: "Delete request",
      });

      await userEvent.click(button);
      expect(deleteRequestMock).toHaveBeenCalledTimes(1);
    });
  });

  describe("render disabled buttons in loading state", () => {
    beforeEach(() => {
      render(
        <DeleteRequestDialog
          cancel={cancelMock}
          deleteRequest={deleteRequestMock}
          isLoading={true}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("Cancel button should be disabled", async () => {
      const dialog = screen.getByRole("dialog");
      const button = within(dialog).getByRole("button", { name: "Cancel" });

      await userEvent.click(button);
      expect(button).toBeDisabled();
    });

    it("Delete button should be disabled", async () => {
      const dialog = screen.getByRole("dialog");
      const button = within(dialog).getByRole("button", {
        name: "Delete request",
      });

      await userEvent.click(button);
      expect(button).toBeDisabled();
    });
  });
});
