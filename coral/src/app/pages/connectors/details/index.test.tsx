import { cleanup, screen } from "@testing-library/react";
import { ConnectorDetailsPage } from "src/app/pages/connectors/details";
import { getConnectorOverview } from "src/domain/connector/connector-api";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const testConnector = "my-connector";
const mockedUsedNavigate = jest.fn();
const mockUseParams = jest.fn();
const mockMatches = jest.fn();

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: () => mockUseParams(),
  useMatches: () => mockMatches(),
  useNavigate: () => mockedUsedNavigate,
}));

jest.mock("src/domain/connector/connector-api");
const mockGetConnectorOverview = getConnectorOverview as jest.MockedFunction<
  typeof getConnectorOverview
>;

describe("ConnectorOverviewPage", () => {
  describe("renders the component handling header and tabs for topic-overview", () => {
    beforeAll(() => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      mockGetConnectorOverview.mockReturnValue({});
      mockUseParams.mockReturnValue({
        connectorName: testConnector,
      });

      mockMatches.mockReturnValue([
        {
          id: "CONNECTOR_OVERVIEW_TAB_ENUM_overview",
        },
      ]);

      customRender(<ConnectorDetailsPage />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("renders the right headline with the prop passed form url", () => {
      const text = screen.getByRole("heading", { name: testConnector });

      expect(text).toBeVisible();
    });

    it("does not redirect", () => {
      expect(mockedUsedNavigate).not.toHaveBeenCalled();
    });
  });

  describe("redirects user if there is no topicName param", () => {
    beforeAll(() => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      mockGetConnectorOverview.mockReturnValue({});
      mockUseParams.mockImplementationOnce(() => {
        return {};
      });
      customRender(<ConnectorDetailsPage />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("does not render the topics name", () => {
      const headline = screen.queryByRole("heading");

      expect(headline).not.toBeInTheDocument();
    });

    it("does redirect user to /connectors", () => {
      expect(mockedUsedNavigate).toHaveBeenCalledWith("/connectors");
    });
  });
});
