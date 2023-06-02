import { SchemaPromotionBanner } from "src/app/features/topics/details/schema/components/SchemaPromotionBanner";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const environment = "TST";
const mockPromoteSchema = jest.fn();
describe("SchemaPromotionBanner", () => {
  const user = userEvent.setup();

  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(
        <SchemaPromotionBanner
          environment={environment}
          promoteSchema={mockPromoteSchema}
        />
      );
    });

    afterAll(cleanup);

    it("shows information about possible promotion", () => {
      const infoText = screen.getByText(
        `This schema has not yet been promoted to the ${environment} environment.`
      );

      expect(infoText).toBeVisible();
    });

    it("shows a button to promote schema", () => {
      const button = screen.getByRole("button", { name: "Promote" });

      expect(button).toBeEnabled();
    });
  });

  describe("enables user to promote a schema", () => {
    beforeEach(() => {
      render(
        <SchemaPromotionBanner
          environment={environment}
          promoteSchema={mockPromoteSchema}
        />
      );
    });

    afterEach(cleanup);

    it("triggeres the promote schema function when user clicks button", async () => {
      const button = screen.getByRole("button", { name: "Promote" });
      await user.click(button);

      expect(mockPromoteSchema).toHaveBeenCalled();
    });
  });
});
