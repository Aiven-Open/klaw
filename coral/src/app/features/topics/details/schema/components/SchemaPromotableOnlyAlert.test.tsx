import { render, cleanup, screen } from "@testing-library/react";
import { SchemaPromotableOnlyAlert } from "src/app/features/topics/details/schema/components/SchemaPromotableOnlyAlert";

describe("SchemaPromotableOnlyAlert", () => {
  beforeAll(() => {
    render(<SchemaPromotableOnlyAlert />);
  });
  afterAll(cleanup);

  it("renders information that schema has to be promoted to higher env", () => {
    const alert = screen.getByTestId("schema-promotable-only-alert");

    expect(alert).toBeVisible();
    expect(alert).toHaveTextContent(
      "Users are not allowed to request a new schema in this environment. To add a schema, promote the schema from a lower environment. You can read more in our documentation."
    );
  });

  it("shows link to documentation", () => {
    const link = screen.getByRole("link", { name: "our documentation" });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      "https://www.klaw-project.io/docs/Concepts/promotion#schema-promotion"
    );
  });
});
