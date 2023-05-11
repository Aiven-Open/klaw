import { cleanup, render, screen } from "@testing-library/react";
import SkipLink from "src/app/layout/skip-link/SkipLink";
import userEvent from "@testing-library/user-event";

describe("SkipLink.tsx", () => {
  let mockedScrollFunction;
  let div: HTMLDivElement;

  beforeEach(() => {
    mockedScrollFunction = vi.fn();
    div = document.createElement("div");

    const mainContentRef = {
      current: div,
    };
    div.scrollIntoView = mockedScrollFunction;
    render(
      <>
        <SkipLink mainContent={mainContentRef} />
        <div id={"main-content"} />
      </>
    );
  });

  afterEach(() => {
    vi.resetAllMocks();
    cleanup();
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

  it("moves focus to the main content when using keyboard", async () => {
    expect(div).not.toHaveAttribute("tabindex", "-1");
    const button = screen.getByRole("button", { name: "Skip to main content" });
    button.focus();
    await userEvent.keyboard("{Enter}");

    expect(div).toHaveAttribute("tabindex", "-1");
    expect(div.scrollIntoView).toHaveBeenCalled();
  });
});
