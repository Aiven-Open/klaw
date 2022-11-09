import Hello from "src/app/pages/Hello";
import { render, cleanup, screen } from "@testing-library/react";

describe("Hello", () => {
  beforeAll(() => {
    render(<Hello />);
  });

  afterAll(cleanup);

  it("shoud render dummy content", () => {
    expect(screen.getByText("Hello")).toBeVisible();
  });
});
