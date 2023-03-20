import { cleanup, render, screen, within } from "@testing-library/react";
import { DeleteRequestModal } from "src/app/features/requests/components/DeleteRequestModal";
import userEvent from "@testing-library/user-event";

describe("DeleteRequestModal", () => {
  const closeMock = jest.fn();
  const deleteRequestMock = jest.fn();

  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(
        <DeleteRequestModal
          close={closeMock}
          deleteRequest={deleteRequestMock}
        />
      );
    });

    afterAll(cleanup);

    it("shows a dialog", () => {
      const modal = screen.getByRole("dialog");

      expect(modal).toBeVisible();
    });

    it("shows a headline for the modal", () => {
      const modal = screen.getByRole("dialog");
      const headline = within(modal).getByRole("heading", {
        name: "Delete request",
      });

      expect(headline).toBeVisible();
    });

    it("shows a text for the modal", () => {
      const modal = screen.getByRole("dialog");
      const textInfo = within(modal).getByText(
        "Are you sure you want to delete the request?"
      );

      expect(textInfo).toBeVisible();
    });

    it("shows a button to close the modal", () => {
      const modal = screen.getByRole("dialog");
      const button = within(modal).getByRole("button", { name: "Close modal" });

      expect(button).toBeEnabled();
    });

    it("shows a button to cancel the deletion of the request", () => {
      const modal = screen.getByRole("dialog");
      const button = within(modal).getByRole("button", { name: "Cancel" });

      expect(button).toBeEnabled();
    });

    it("shows a button to confirm deletion of the request", () => {
      const modal = screen.getByRole("dialog");
      const button = within(modal).getByRole("button", {
        name: "Delete request",
      });

      expect(button).toBeEnabled();
    });
  });

  describe("handles user interaction", () => {
    beforeEach(() => {
      render(
        <DeleteRequestModal
          close={closeMock}
          deleteRequest={deleteRequestMock}
        />
      );
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("calls the given close function when user clicks close button", async () => {
      const modal = screen.getByRole("dialog");
      const button = within(modal).getByRole("button", { name: "Close modal" });

      await userEvent.click(button);
      expect(closeMock).toHaveBeenCalledTimes(1);
    });

    it("calls the given close function when user clicks button to cancel the deletion of the request", async () => {
      const modal = screen.getByRole("dialog");
      const button = within(modal).getByRole("button", { name: "Cancel" });

      await userEvent.click(button);
      expect(closeMock).toHaveBeenCalledTimes(1);
    });

    it("calls the given deleteRequest function when user clicks button to confirm deletion of the request", async () => {
      const modal = screen.getByRole("dialog");
      const button = within(modal).getByRole("button", {
        name: "Delete request",
      });

      await userEvent.click(button);
      expect(deleteRequestMock).toHaveBeenCalledTimes(1);
    });
  });
});
