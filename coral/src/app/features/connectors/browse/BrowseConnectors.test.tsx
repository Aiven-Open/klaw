import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import BrowseConnectors from "src/app/features/connectors/browse/BrowseConnectors";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { Connector, getConnectors } from "src/domain/connector";
import { getSyncConnectorsEnvironments } from "src/domain/environment";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { tabNavigateTo } from "src/services/test-utils/tabbing";
import { getTeams } from "src/domain/team";

jest.mock("src/domain/connector/connector-api.ts");
jest.mock("src/domain/team/team-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetConnectors = getConnectors as jest.MockedFunction<
  typeof getConnectors
>;

const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;

const mockGetSyncConnectorsEnvironments =
  getSyncConnectorsEnvironments as jest.MockedFunction<
    typeof getSyncConnectorsEnvironments
  >;

const mockConnectors: Connector[] = [
  {
    sequence: 2,
    connectorId: 1001,
    connectorName: "test_connector_1",
    environmentId: "4",
    teamName: "Dev",
    teamId: 1,
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: ["DEV"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
  },
  {
    sequence: 2,
    connectorId: 1002,
    connectorName: "test_connector_2",
    environmentId: "4",
    teamName: "Ospo",
    teamId: 2,
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: ["DEV", "TST"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
  },
  {
    sequence: 2,
    connectorId: 1003,
    connectorName: "test_connector_3",
    environmentId: "4",
    teamName: "Infra",
    teamId: 3,
    allPageNos: ["1"],
    totalNoPages: "1",
    currentPage: "1",
    environmentsList: ["TST"],
    description: "test connect desc",
    showEditConnector: false,
    showDeleteConnector: false,
    connectorDeletable: false,
  },
];

const mockResponseDefault = {
  totalPages: 1,
  currentPage: 1,
  entries: mockConnectors,
};

const defaultApiParams = {
  pageNo: "1",
  env: "ALL",
  connectornamesearch: undefined,
};

const mockTeams = [
  {
    teamname: "Ospo",
    teammail: "ospo@aiven.io",
    teamphone: "003157843623",
    contactperson: "Ospo Office",
    tenantId: 101,
    teamId: 1003,
    app: "",
    showDeleteTeam: false,
    tenantName: "default",
    envList: ["ALL"],
  },
  {
    teamname: "DevRel",
    teammail: "devrel@aiven.io",
    teamphone: "003146237478",
    contactperson: "Dev Rel",
    tenantId: 101,
    teamId: 1004,
    app: "",
    showDeleteTeam: false,
    tenantName: "default",
    envList: ["ALL"],
  },
];

const mockEnvironments = [
  createEnvironment({
    id: "1",
    name: "DEV",
  }),
  createEnvironment({
    id: "2",
    name: "TST",
  }),
];

describe("BrowseConnectors.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetConnectors.mockResolvedValue(mockResponseDefault);

      customRender(<BrowseConnectors />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Connector table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Connectors overview, page 1 of 1",
      });

      expect(table).toBeVisible();
    });

    it("shows Connector names row headers", () => {
      const table = screen.getByRole("table", {
        name: "Connectors overview, page 1 of 1",
      });

      const rowHeader = within(table).getByRole("cell", {
        name: mockResponseDefault.entries[0].connectorName,
      });
      expect(rowHeader).toBeVisible();
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
      mockGetTeams.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetConnectors.mockResolvedValue({
        ...mockResponseDefault,
        totalPages: 3,
        currentPage: 2,
      });

      customRender(<BrowseConnectors />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Connector table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Connectors overview, page 2 of 3",
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
      mockGetTeams.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetConnectors.mockResolvedValue({
        ...mockResponseDefault,
        totalPages: 4,
        currentPage: 2,
      });

      customRender(<BrowseConnectors />, {
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

      expect(mockGetConnectors).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        pageNo: "3",
      });
    });
  });

  describe("handles user filtering connectors by Team", () => {
    const filterByTeamLabel = "Filter by team";

    beforeEach(async () => {
      mockGetTeams.mockResolvedValue(mockTeams);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetConnectors.mockResolvedValue(mockResponseDefault);

      customRender(<BrowseConnectors />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a select element for Team with `ALL` preselected", async () => {
      const select = await screen.findByRole("combobox", {
        name: filterByTeamLabel,
      });

      expect(select).toHaveValue("ALL");
      expect(select).toHaveDisplayValue("All teams");
    });

    it("changes active selected option when user selects `Ospo`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByTeamLabel,
      });
      const option = within(select).getByRole("option", {
        name: "Ospo",
      });
      expect(select).toHaveValue("ALL");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue("1003");
      expect(select).toHaveDisplayValue("Ospo");
    });

    it("fetches new data when user selects `Ospo`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByTeamLabel,
      });
      const option = within(select).getByRole("option", {
        name: "Ospo",
      });

      await userEvent.selectOptions(select, option);

      expect(mockGetConnectors).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        teamId: 1003,
      });
    });
  });

  describe("handles user filtering connectors by Environment", () => {
    const filterByEnvironmentLabel = "Filter by Environment";

    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue(mockEnvironments);
      mockGetConnectors.mockResolvedValue(mockResponseDefault);

      customRender(<BrowseConnectors />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a select element for Environments with `ALL` preselected", async () => {
      const select = await screen.findByRole("combobox", {
        name: filterByEnvironmentLabel,
      });

      expect(select).toHaveValue("ALL");
      expect(select).toHaveDisplayValue("All Environments");
    });

    it("changes active selected option when user selects `DEV`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByEnvironmentLabel,
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });
      expect(select).toHaveValue("ALL");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue("1");
      expect(select).toHaveDisplayValue("DEV");
    });

    it("fetches new data when user selects `DEV`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByEnvironmentLabel,
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });

      await userEvent.selectOptions(select, option);

      expect(mockGetConnectors).toHaveBeenNthCalledWith(2, {
        ...defaultApiParams,
        env: "1",
      });
    });
  });

  describe("handles user searching by connector name with search input", () => {
    const testSearchInput = "My search name";
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetConnectors.mockResolvedValue(mockResponseDefault);
      customRender(<BrowseConnectors />, {
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
      const input = screen.getByRole("search");
      expect(input).toHaveValue("");

      await userEvent.type(input, testSearchInput);

      expect(input).toHaveValue(testSearchInput);

      await waitFor(() =>
        expect(mockGetConnectors).toHaveBeenNthCalledWith(2, {
          ...defaultApiParams,
          connectornamesearch: testSearchInput,
        })
      );
    });

    it("enables user to navigate to search input with keyboard", async () => {
      const input = screen.getByRole("search");

      expect(input).toHaveValue("");

      await tabNavigateTo({ targetElement: input });

      expect(input).toHaveFocus();
    });
  });
});
