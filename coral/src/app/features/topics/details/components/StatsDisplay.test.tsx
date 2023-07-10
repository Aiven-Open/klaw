import { cleanup, render, screen } from "@testing-library/react";
import StatsDisplay from "src/app/features/topics/details/components/StatsDisplay";

describe("StatsDisplay", () => {
  describe("shows stats for numbers", () => {
    afterEach(cleanup);

    it("renders a loading state for amount", () => {
      render(<StatsDisplay isLoading={true} amount={0} entity={"Test"} />);

      const number = screen.queryByText("10");
      const loadingInfo = screen.getByText("Loading information");
      const name = screen.getByText("Test");

      expect(number).not.toBeInTheDocument();
      expect(loadingInfo).toBeVisible();
      expect(loadingInfo).toHaveClass("visually-hidden");
      expect(name).toBeVisible();
    });

    it("renders correct data for amount", () => {
      render(<StatsDisplay isLoading={false} amount={10} entity={"Test"} />);

      const number = screen.getByText("10");
      const name = screen.getByText("Test");

      expect(number).toBeVisible();
      expect(name).toBeVisible();
    });
  });

  describe("shows stats for chip (status)", () => {
    afterEach(cleanup);

    it("renders a loading state for chip", () => {
      render(
        <StatsDisplay
          isLoading={true}
          chip={{ status: "info", text: "STATUS" }}
          entity={"Test"}
        />
      );

      const loadingInfo = screen.getByText("Loading information");
      const chip = screen.queryByText("STATUS");
      const name = screen.getByText("Test");

      expect(chip).not.toBeInTheDocument();
      expect(loadingInfo).toBeVisible();
      expect(loadingInfo).toHaveClass("visually-hidden");
      expect(name).toBeVisible();
    });

    it("shows correct chip for status 'info'", () => {
      render(
        <StatsDisplay
          isLoading={false}
          chip={{ status: "info", text: "STATUS" }}
          entity={"Test"}
        />
      );

      const chip = screen.getByText("STATUS");
      const name = screen.getByText("Test");

      expect(chip).toBeVisible();
      expect(name).toBeVisible();
    });
  });

  describe("handles no or double data", () => {
    const consoleErrorOriginal = console.error;

    beforeEach(() => {
      console.error = jest.fn();
    });

    afterEach(() => {
      console.error = consoleErrorOriginal;
      cleanup();
    });

    it("shows the right information for missing chip and number", () => {
      render(<StatsDisplay isLoading={false} entity={"Test"} />);

      const loadingInfo = screen.queryByText("Loading information");
      const name = screen.getByText("Test");

      expect(loadingInfo).not.toBeInTheDocument();
      expect(name).toBeVisible();
      expect(console.error).toHaveBeenCalledWith("Define a chip or amount.");
    });

    it("shows the right information for passing chip and amount at the same time", () => {
      render(
        <StatsDisplay
          isLoading={false}
          chip={{ status: "info", text: "STATUS" }}
          amount={11}
          entity={"Test"}
        />
      );

      const name = screen.getByText("Test");

      expect(name).toBeVisible();
      expect(console.error).toHaveBeenCalledWith(
        "Define only either a chip or an amount."
      );
    });
  });
});
