import { cleanup, render, screen } from "@testing-library/react";
import StatsDisplay from "src/app/features/topics/details/components/StatsDisplay";

describe("StatsDisplay", () => {
  afterEach(cleanup);
  it("renders correct data (no data)", () => {
    render(<StatsDisplay amount={0} entity={"Test"} />);

    const number = screen.getByText("0");
    const name = screen.getByText("Test");

    expect(number).toBeVisible();
    expect(name).toBeVisible();
  });

  it("renders correct data (with data)", () => {
    render(<StatsDisplay amount={10} entity={"Test"} />);

    const number = screen.getByText("10");
    const name = screen.getByText("Test");

    expect(number).toBeVisible();
    expect(name).toBeVisible();
  });
});
