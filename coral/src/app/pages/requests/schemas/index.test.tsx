import { getSchemaRequests } from "src/domain/schema-request";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import SchemaRequestsPage from "src/app/pages/requests/schemas/index";
import { cleanup, screen, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { getEnvironmentsForSchemaRequest } from "src/domain/environment";

vi.mock("src/domain/environment/environment-api.ts");
vi.mock("src/domain/schema-request/schema-request-api.ts");

const mockGetSchemaRegistryEnvironments =
  getEnvironmentsForSchemaRequest as vi.MockedFunction<
    typeof getEnvironmentsForSchemaRequest
  >;
const mockGetSchemaRequests = getSchemaRequests as vi.MockedFunction<
  typeof getSchemaRequests
>;

describe("SchemaRequestPage", () => {
  beforeAll(async () => {
    mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
    mockGetSchemaRequests.mockResolvedValue({
      entries: [],
      totalPages: 1,
      currentPage: 1,
    });

    customRender(<SchemaRequestsPage />, {
      queryClient: true,
      memoryRouter: true,
    });
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
  });

  afterAll(cleanup);

  it("shows a preview banner to inform users about the early version of the view", () => {
    const previewBanner = screen.getByLabelText("Preview disclaimer");

    expect(previewBanner).toBeVisible();
    expect(previewBanner).toHaveTextContent(
      "You are viewing a preview of the redesigned user interface. You are one of our early reviewers, and your feedback will help us improve the product. You can always go back to the old interface."
    );
  });

  it("shows link back back to the original Klaw app for this view in the preview banner", () => {
    const previewBanner = screen.getByLabelText("Preview disclaimer");
    const link = within(previewBanner).getByRole("link", {
      name: "old interface",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute("href", "/mySchemaRequests");
  });

  it("renders the schema request view", () => {
    const emptyRequests = screen.getByText(
      "No Schema request matched your criteria."
    );

    expect(emptyRequests).toBeVisible();
  });
});
