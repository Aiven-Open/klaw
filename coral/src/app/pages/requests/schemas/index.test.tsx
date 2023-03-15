import { getSchemaRequests } from "src/domain/schema-request";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import SchemaRequestsPage from "src/app/pages/requests/schemas/index";
import { cleanup, screen } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";

jest.mock("src/domain/schema-request/schema-request-api.ts");

const mockGetSchemaRequests = getSchemaRequests as jest.MockedFunction<
  typeof getSchemaRequests
>;

describe("SchemaRequestPage", () => {
  beforeAll(() => {
    mockGetSchemaRequests.mockResolvedValue({
      entries: [],
      totalPages: 1,
      currentPage: 1,
    });

    customRender(<SchemaRequestsPage />, {
      queryClient: true,
    });
  });

  afterAll(cleanup);

  it("renders the schema request view", async () => {
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    const emptyRequests = screen.getByText(
      "No Schema request matched your criteria."
    );

    expect(emptyRequests).toBeVisible();
  });
});
