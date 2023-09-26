import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import SchemaRegistryEnvironments from "src/app/features/configuration/environments/SchemaRegistry/SchemaRegistryEnvironments";
import { getPaginatedEnvironmentsForSchema } from "src/domain/environment";
import { transformPaginatedEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { EnvironmentPaginatedApiResponse } from "src/domain/environment/environment-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabNavigateTo } from "src/services/test-utils/tabbing";

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const defaultApiParams = {
  pageNo: "1",
  searchEnvParam: undefined,
};

jest.mock("src/domain/environment/environment-api.ts");

const mockGetPaginatedEnvironmentsForSchema =
  getPaginatedEnvironmentsForSchema as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForSchema
  >;

const mockedEnvironmentsResponse: EnvironmentPaginatedApiResponse =
  transformPaginatedEnvironmentApiResponse([
    {
      id: "9",
      name: "TST_SCH",
      type: "schemaregistry",
      tenantId: 101,
      clusterId: 7,
      tenantName: "default",
      clusterName: "TST_SCHEMA",
      envStatus: "ONLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      envStatusTimeString: "2023-09-21T11:46:15.664615239",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 2,
      associatedEnv: {
        id: "2",
        name: "TST",
      },
      otherParams: "",
      params: {},
    },
    {
      id: "3",
      name: "DEV",
      type: "schemaregistry",
      tenantId: 101,
      clusterId: 10,
      tenantName: "default",
      clusterName: "DEV_CLS",
      envStatus: "ONLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      envStatusTimeString: "2023-09-21T11:46:15.664615239",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 2,
      associatedEnv: {
        id: "1",
        name: "DEV",
      },
      otherParams: "",
      params: {},
    },
  ]);

describe("SchemaRegistryEnvironments.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetPaginatedEnvironmentsForSchema.mockResolvedValue(
        mockedEnvironmentsResponse
      );

      customRender(<SchemaRegistryEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Schema Registry Environments table with information about the pages", () => {
      const table = screen.getByRole("table", {
        name: "Schema Registry Environments overview, page 1 of 1",
      });

      expect(table).toBeVisible();
    });

    it("shows Environment names row headers", async () => {
      const table = screen.getByRole("table", {
        name: "Schema Registry Environments overview, page 1 of 1",
      });

      const rowHeader = within(table).getByRole("cell", {
        name: mockedEnvironmentsResponse.entries[0].name,
      });
      expect(rowHeader).toBeVisible();
    });

    it("render the correct amount of rows", async () => {
      const rows = screen.getAllByRole("row");

      expect(rows).toHaveLength(3);
    });

    it("does not render the pagination", () => {
      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).not.toBeInTheDocument();
    });
  });

  describe("handles successful response with three pages", () => {
    beforeAll(async () => {
      mockGetPaginatedEnvironmentsForSchema.mockResolvedValue({
        ...mockedEnvironmentsResponse,
        totalPages: 3,
        currentPage: 2,
      });

      customRender(<SchemaRegistryEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Schema Registry Environments table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Schema Registry Environments overview, page 2 of 3",
      });

      expect(table).toBeVisible();
    });

    it("renders the pagination with information about the pages", () => {
      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 2 of 3",
      });

      expect(pagination).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetPaginatedEnvironmentsForSchema.mockResolvedValue({
        ...mockedEnvironmentsResponse,
        totalPages: 4,
        currentPage: 2,
      });

      customRender(<SchemaRegistryEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 2 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 2 of 4"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 3",
      });

      await userEvent.click(pageTwoButton);

      expect(mockGetPaginatedEnvironmentsForSchema).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "3",
      });
    });
  });

  describe("handles user searching with search input", () => {
    const testSearchInput = "My search name";

    beforeEach(async () => {
      mockGetPaginatedEnvironmentsForSchema.mockResolvedValue(
        mockedEnvironmentsResponse
      );

      customRender(<SchemaRegistryEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("fetches new data when when user enters text in input", async () => {
      const search = screen.getByRole("search", {
        name: "Search Schema Registry Environment",
      });
      expect(search).toHaveValue("");

      await userEvent.type(search, testSearchInput);

      expect(search).toHaveValue(testSearchInput);

      await waitFor(() =>
        expect(mockGetPaginatedEnvironmentsForSchema).toHaveBeenCalledWith({
          ...defaultApiParams,
          searchEnvParam: testSearchInput,
        })
      );
    });

    it("enables user to navigate to search input with keyboard", async () => {
      const search = screen.getByRole("search", {
        name: "Search Schema Registry Environment",
      });

      expect(search).toHaveValue("");

      await tabNavigateTo({ targetElement: search });

      expect(search).toHaveFocus();
    });
  });
});
