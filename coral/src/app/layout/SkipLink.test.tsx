import { cleanup, render, screen } from "@testing-library/react";
import SkipLink from "src/app/layout/SkipLink";
import userEvent from "@testing-library/user-event";

describe("SkipLink.tsx", () => {
  const mockedScrollFunction = jest.fn();
  const div = document.createElement("div");
  div.scrollIntoView = mockedScrollFunction;

  const mainContentRef = {
    current: div,
  };
  beforeEach(() => {
    render(
      <>
        <SkipLink mainContent={mainContentRef} />
        <div id={"main-content"} />
      </>
    );
  });

  afterEach(() => {
    cleanup();
    jest.clearAllMocks();
  });

  it("shows a button to skip to main content", () => {
    const button = screen.getByRole("button", { name: "Skip to main content" });

    expect(button).toBeEnabled();
  });

  it("moves focus to the main content when clicking", async () => {
    expect(div).not.toHaveAttribute("tabindex", "-1");
    const button = screen.getByRole("button", { name: "Skip to main content" });
    await userEvent.click(button);

    expect(div).toHaveAttribute("tabindex", "-1");
    expect(div.scrollIntoView).toHaveBeenCalled();
  });
});
