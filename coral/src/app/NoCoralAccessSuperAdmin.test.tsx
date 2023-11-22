import { cleanup, render, screen, within } from "@testing-library/react";
import { NoCoralAccessSuperadmin } from "src/app/components/NoCoralAccessSuperadmin";

describe("NoCoralAccessSuperadmin.tsx", () => {
  beforeAll(() => {
    render(<NoCoralAccessSuperadmin />);
  });

  afterAll(cleanup);

  it("shows an dialog for accessing new user interfacne", () => {
    const dialog = screen.getByRole("dialog", {
      name: "You're currently logged in as superadmin.",
    });

    expect(dialog).toBeVisible();
  });

  it("informs user that they can access new user interface with user account", () => {
    const dialog = screen.getByRole("dialog", {
      name: "You're currently logged in as superadmin.",
    });

    const text = within(dialog).getByText(
      "To experience the new user interface, switch to your user account."
    );

    expect(text).toBeVisible();
  });

  it("informs user that they can continue to old UI and stay superadmin", () => {
    const dialog = screen.getByRole("dialog", {
      name: "You're currently logged in as superadmin.",
    });

    const text = within(dialog).getByText(
      "To continue as superadmin, go to the old interface."
    );

    expect(text).toBeVisible();
  });

  it("shows a link to login page", () => {
    const link = screen.getByRole("link", {
      name: "Login as user",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute("href", "/login");
  });

  it("shows a link to the start page", () => {
    const link = screen.getByRole("link", {
      name: "Go to old interface",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute("href", "/");
  });
});
