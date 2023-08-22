import { OpenSchemaRequestAlert } from "src/app/features/topics/details/schema/components/OpenSchemaRequestAlert";
import { screen } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const testTopicName = "my-topic";
describe("OpenSchemaRequestAlert", () => {
  beforeAll(() => {
    customRender(<OpenSchemaRequestAlert topicName={testTopicName} />, {
      memoryRouter: true,
    });
  });

  it("shows information about pending request", () => {
    const text = screen.getByText(
      `You cannot request a schema at this time. A schema request for ${testTopicName} is already in progress.`
    );

    expect(text).toBeVisible();
  });

  it("shows link to open request", () => {
    const link = screen.getByRole("link", { name: "View request" });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/requests/schemas?status=CREATED&page=1&search=${testTopicName}`
    );
  });
});
