import { cleanup, render, screen, within } from "@testing-library/react";
import { AuthenticationRequiredAlert } from "src/app/components/AuthenticationRequiredAlert";

describe("AuthenticationRequiredAlert.tsx", () => {
  beforeEach(() => {
    render(<AuthenticationRequiredAlert />);
  });
  afterEach(() => {
    cleanup();
  });

  it("shows an alert about the expired auth session", () => {
    const alert = screen.getByRole("alertdialog", {
      name: "Authentication session expired",
    });

    expect(alert).toBeVisible();
  });

  it("informs user that they are redirected to the login page", () => {
    const alert = screen.getByRole("alertdialog", {
      name: "Authentication session expired",
    });

    const text = within(alert).getByText("Redirecting to login page.");

    expect(text).toBeVisible();
  });
});
