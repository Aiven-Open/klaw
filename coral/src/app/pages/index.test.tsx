import HomePage from "src/app/pages";
import { render, cleanup, screen } from "@testing-library/react";

describe("HomePage", () => {
  beforeAll(() => {
    render(<HomePage />);
  });

  afterAll(cleanup);

  it("renders dummy content", () => {
    const heading = screen.getByRole("heading", { name: "Index" });

    expect(heading).toBeVisible();
  });
});
