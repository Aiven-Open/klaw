import HomePage from "src/app/pages";
import { cleanup, screen } from "@testing-library/react";
import { renderWithQueryClient } from "src/services/test-utils";

describe("HomePage", () => {
  beforeAll(() => {
    renderWithQueryClient(<HomePage />);
  });

  afterAll(cleanup);

  it("renders dummy content", () => {
    const heading = screen.getByRole("heading", { name: "Index" });

    expect(heading).toBeVisible();
  });
});
