import { cleanup, screen } from "@testing-library/react/pure";
import EnvironmentsPage from "src/app/pages/configuration/environments";
import {
  getPaginatedEnvironmentsForConnector,
  getPaginatedEnvironmentsForSchema,
  getPaginatedEnvironmentsForTopicAndAcl,
} from "src/domain/environment";
import { EnvironmentPaginatedApiResponse } from "src/domain/environment/environment-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockMatches = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useMatches: () => mockMatches(),
}));

const mockedResponse: EnvironmentPaginatedApiResponse = {
  totalPages: 0,
  currentPage: 0,
  totalEnvs: 0,
  entries: [],
};

jest.mock("src/domain/environment/environment-api.ts");

const mockGetPaginatedEnvironmentsForTopicAndAcl =
  getPaginatedEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForTopicAndAcl
  >;
const mockGetPaginatedEnvironmentsForSchema =
  getPaginatedEnvironmentsForSchema as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForSchema
  >;
const mockGetPaginatedEnvironmentsForConnector =
  getPaginatedEnvironmentsForConnector as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForConnector
  >;

describe("Environments page", () => {
  beforeEach(() => {
    mockGetPaginatedEnvironmentsForTopicAndAcl.mockResolvedValue(
      mockedResponse
    );
    mockGetPaginatedEnvironmentsForSchema.mockResolvedValue(mockedResponse);
    mockGetPaginatedEnvironmentsForConnector.mockResolvedValue(mockedResponse);
  });
  describe("renders Environments page with correct text", () => {
    beforeAll(() => {
      mockMatches.mockImplementation(() => [
        {
          id: "ENVIRONMENTS_TAB_ENUM_kafka",
        },
      ]);

      customRender(<EnvironmentsPage />, {
        memoryRouter: true,
        queryClient: true,
      });
    });

    afterAll(cleanup);

    it("renders a headline", () => {
      const headline = screen.getByRole("heading", {
        name: "Environments",
      });

      expect(headline).toBeVisible();
    });
  });
});
