import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, screen } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { getAllEnvironmentsForConnector } from "src/domain/environment";
import { getConnectorRequests } from "src/domain/connector/connector-api";
import ConnectorRequestsPage from "src/app/pages/requests/connectors/index";
import { getTeams } from "src/domain/team";

jest.mock("src/domain/team/team-api.ts");
jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/connector/connector-api.ts");

const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;
const mockGetSyncConnectorsEnvironments =
  getAllEnvironmentsForConnector as jest.MockedFunction<
    typeof getAllEnvironmentsForConnector
  >;
const mockGetConnectorRequests = getConnectorRequests as jest.MockedFunction<
  typeof getConnectorRequests
>;

describe("ConnectorRequestsPage", () => {
  beforeAll(async () => {
    mockGetTeams.mockResolvedValue([]);
    mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
    mockGetConnectorRequests.mockResolvedValue({
      entries: [],
      totalPages: 1,
      currentPage: 1,
    });

    customRender(<ConnectorRequestsPage />, {
      queryClient: true,
      memoryRouter: true,
      aquariumContext: true,
    });
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
  });

  afterAll(cleanup);

  it("renders the Connector request view", () => {
    const emptyRequests = screen.getByText(
      "No Connector request matched your criteria."
    );

    expect(emptyRequests).toBeVisible();
  });
});
