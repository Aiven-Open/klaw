import HelloPage from "./index";
import { render, cleanup, screen } from "@testing-library/react";

describe("HelloPage", () => {
  beforeEach(() => {
    render(<HelloPage />);
  });

  afterEach(() => {
    cleanup();
  });
  it("shoud render dummy content", () => {
    expect(screen.getByText("Hello")).toBeVisible();
  });
});
