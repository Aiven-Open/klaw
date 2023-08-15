import { render, cleanup, screen } from "@testing-library/react";
import { SchemaPromotableOnlyAlert } from "src/app/features/topics/details/schema/components/SchemaPromotableOnlyAlert";

describe("SchemaPromotableOnlyAlert", () => {
  describe("renders the default alert", () => {
    beforeAll(() => {
      render(<SchemaPromotableOnlyAlert />);
    });
    afterAll(cleanup);

    it("renders information that schema has to be promoted to higher env", () => {
      const alert = screen.getByTestId("schema-promotable-only-alert");

      expect(alert).toBeVisible();
      expect(alert).toHaveTextContent(
        "Schemas are created in lower environments and promoted to higher environment. To add a schema to this topic, create a request in the lower environment and promote it to the higher one. Learn more."
      );
    });

    it("shows link to documentation", () => {
      const link = screen.getByRole("link", { name: "Learn more" });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        "https://www.klaw-project.io/docs/Concepts/promotion#schema-promotion"
      );
    });
  });

  describe("renders the alert for a new schema version", () => {
    beforeAll(() => {
      render(<SchemaPromotableOnlyAlert isNewVersionRequest={true} />);
    });

    afterAll(cleanup);

    it("renders information that schema has to be promoted to higher env", () => {
      const alert = screen.getByTestId("schema-promotable-only-alert");

      expect(alert).toBeVisible();
      expect(alert).toHaveTextContent(
        "You can't create a new schema or new version for it here. Create your request in the lowest environment and then promote it upwards. Learn more."
      );
    });

    it("shows link to documentation", () => {
      const link = screen.getByRole("link", { name: "Learn more" });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        "https://www.klaw-project.io/docs/Concepts/promotion#schema-promotion"
      );
    });
  });
});
