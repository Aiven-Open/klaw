import { DisabledButtonTooltip } from "src/app/components/DisabledButtonTooltip";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const testTooltip = "There is a pending request.";
const testChild = "Edit this!";
describe("DisabledButtonTooltip", () => {
  const user = userEvent.setup();

  describe("renders all necessary elements for default (button)", () => {
    beforeAll(() => {
      render(
        <DisabledButtonTooltip tooltip={testTooltip}>
          {testChild}
        </DisabledButtonTooltip>
      );
    });
    afterAll(cleanup);

    it("shows a disabled-looking button", () => {
      const button = screen.getByRole("button");

      expect(button).toHaveAttribute("aria-disabled", "true");
      expect(button.parentElement).toHaveClass("buttonWrapper");
    });

    it("renders the tooltip text accessible for screen reader", () => {
      const button = screen.getByRole("button");

      expect(button).toHaveAccessibleName(`${testChild}. ${testTooltip}`);
    });

    it("shows a tooltip for visual user", async () => {
      await user.tab();

      const tooltip = screen.getByRole("tooltip");

      // note: this is _not_ use able for the screen reader,
      // see comments in component. Since we can't give the
      // tooltip a testid, (mis)using the role for querying
      // is used, but it's misleading, since it implied accessibility
      expect(tooltip).toBeVisible();

      await user.tab();

      expect(tooltip).not.toBeInTheDocument();
    });
  });

  describe("renders all necessary elements for role link", () => {
    beforeAll(() => {
      render(
        <DisabledButtonTooltip tooltip={testTooltip} role={"link"}>
          {testChild}
        </DisabledButtonTooltip>
      );
    });
    afterAll(cleanup);

    it("shows a disabled-looking button as link", () => {
      const link = screen.getByRole("link");

      expect(link).toHaveAttribute("aria-disabled", "true");
      expect(link.parentElement).toHaveClass("buttonWrapper");
    });

    it("renders the tooltip text accessible for screen reader", () => {
      const button = screen.getByRole("link");

      expect(button).toHaveAccessibleName(`${testChild}. ${testTooltip}`);
    });

    it("shows a tooltip for visual user", async () => {
      await user.tab();

      const tooltip = screen.getByRole("tooltip");

      // note: this is _not_ use able for the screen reader,
      // see comments in component. Since we can't give the
      // tooltip a testid, (mis)using the role for querying
      // is used, but it's misleading, since it implied accessibility
      expect(tooltip).toBeVisible();

      await user.tab();

      expect(tooltip).not.toBeInTheDocument();
    });
  });
});
