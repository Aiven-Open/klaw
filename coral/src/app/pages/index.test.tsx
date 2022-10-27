import HomePage from "src/app/pages";
import { render, cleanup, screen } from "@testing-library/react";

describe("HomePage", () => {
  beforeEach(() => {
    render(<HomePage />);
  });

  afterEach(() => {
    cleanup();
  });
  it("shoud render dummy content", () => {
    expect(screen.getByText("Index")).toBeVisible();
  });
});
