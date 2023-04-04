import { cleanup } from "@testing-library/react";
import transformConnectorRequestApiResponse from "src/domain/connector/connector-transformer";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { ConnectorRequests } from "src/app/features/requests/connectors/ConnectorRequests";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { getEnvironments } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/app/features/requests/schemas/utils/mocked-api-responses";
import { getConnectorRequests } from "src/domain/connector";

jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/connector/connector-api.ts");

const mockGetConnectorEnvironmentRequest =
  getEnvironments as jest.MockedFunction<typeof getEnvironments>;

const mockGetConnectorRequests = getConnectorRequests as jest.MockedFunction<
  typeof getConnectorRequests
>;

const mockGetConnectorRequestsResponse = transformConnectorRequestApiResponse([
  {
    connectorName: "test-connector-1",
    environment: "1",
    teamname: "NCC1701D",
    remarks: "asap",
    description: "This connector is for test",
    environmentName: "BRG",
    connectorId: 1000,
    requestOperationType: "CREATE",
    requestor: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    requestStatus: "CREATED",
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
    editable: true,
    deletable: true,
    connectorConfig: "",
  },
]);

describe("ConnectorRequests", () => {
  beforeEach(() => {
    mockIntersectionObserver();
    mockGetConnectorEnvironmentRequest.mockResolvedValue(
      mockedEnvironmentResponse
    );
    mockGetConnectorRequests.mockResolvedValue(
      mockGetConnectorRequestsResponse
    );
  });

  afterEach(() => {
    cleanup();
    jest.resetAllMocks();
  });

  it("makes a request to the api to get the teams connector requests", () => {
    customRender(<ConnectorRequests />, {
      queryClient: true,
      memoryRouter: true,
    });
    expect(getConnectorRequests).toBeCalledTimes(1);
  });
});
