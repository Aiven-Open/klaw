import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import KafkaEnvironments from "src/app/features/configuration/environments/Kafka/KafkaEnvironments";
import { getPaginatedEnvironmentsForTopicAndAcl } from "src/domain/environment";
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

const mockGetPaginatedEnvironmentsForTopicAndAcl =
  getPaginatedEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForTopicAndAcl
  >;

const mockedEnvironmentsResponse: EnvironmentPaginatedApiResponse =
  transformPaginatedEnvironmentApiResponse([
    {
      id: "7",
      name: "UAT",
      type: "kafka",
      tenantId: 101,
      clusterId: 1,
      tenantName: "default",
      clusterName: "DEV",
      envStatus: "OFFLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      otherParams: "",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 4,
      params: {
        defaultPartitions: "2",
        maxPartitions: "2",
        partitionsList: ["1", "2 (default)"],
        defaultRepFactor: "1",
        maxRepFactor: "1",
        replicationFactorList: ["1 (default)"],
        topicPrefix: [""],
        topicSuffix: [""],
        topicRegex: ["Dev-.*"],
        applyRegex: true,
      },
    },
    {
      id: "1",
      name: "DEV",
      type: "kafka",
      tenantId: 101,
      clusterId: 9,
      tenantName: "default",
      clusterName: "DEV_CLS",
      envStatus: "ONLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      otherParams: "",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 4,
      associatedEnv: {
        id: "3",
        name: "DEV",
      },
      params: {
        defaultPartitions: "2",
        maxPartitions: "4",
        partitionsList: ["1", "2 (default)", "3", "4"],
        defaultRepFactor: "2",
        maxRepFactor: "4",
        replicationFactorList: ["1", "2 (default)", "3", "4"],
        topicPrefix: [""],
        topicSuffix: [""],
        topicRegex: [".*-(RES|COM|HR)-.*"],
        applyRegex: false,
      },
    },
    {
      id: "2",
      name: "TST",
      type: "kafka",
      tenantId: 101,
      clusterId: 4,
      tenantName: "default",
      clusterName: "TST",
      envStatus: "ONLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      otherParams:
        "default.partitions=2,max.partitions=2,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 4,
      associatedEnv: {
        id: "9",
        name: "TST_SCH",
      },
      params: {
        defaultPartitions: "2",
        maxPartitions: "2",
        partitionsList: ["1", "2 (default)"],
        defaultRepFactor: "1",
        maxRepFactor: "1",
        replicationFactorList: ["1 (default)"],
        topicPrefix: [""],
        topicSuffix: [""],
        topicRegex: [],
        applyRegex: false,
      },
    },
    {
      id: "5",
      name: "PRD",
      type: "kafka",
      tenantId: 101,
      clusterId: 1,
      tenantName: "default",
      clusterName: "DEV",
      envStatus: "OFFLINE",
      envStatusTime: "2023-09-21T11:47:15.664615239",
      otherParams: "",
      showDeleteEnv: false,
      totalNoPages: "1",
      currentPage: "1",
      allPageNos: ["1"],
      totalRecs: 4,
      params: {
        defaultPartitions: "2",
        maxPartitions: "2",
        partitionsList: ["1", "2 (default)"],
        defaultRepFactor: "1",
        maxRepFactor: "1",
        replicationFactorList: ["1 (default)"],
        topicPrefix: [""],
        topicSuffix: [""],
        topicRegex: [""],
        applyRegex: false,
      },
    },
  ]);

describe("KafkaEnvironments.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetPaginatedEnvironmentsForTopicAndAcl.mockResolvedValue(
        mockedEnvironmentsResponse
      );

      customRender(<KafkaEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Kafka Environments table with information about the pages", () => {
      const table = screen.getByRole("table", {
        name: "Kafka Environments overview, page 1 of 1",
      });

      expect(table).toBeVisible();
    });

    it("shows Environment names row headers", async () => {
      const table = screen.getByRole("table", {
        name: "Kafka Environments overview, page 1 of 1",
      });

      const rowHeader = within(table).getByRole("cell", {
        name: mockedEnvironmentsResponse.entries[0].name,
      });
      expect(rowHeader).toBeVisible();
    });

    it("render the correct amount of rows", async () => {
      const rows = screen.getAllByRole("row");

      expect(rows).toHaveLength(5);
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
      mockGetPaginatedEnvironmentsForTopicAndAcl.mockResolvedValue({
        ...mockedEnvironmentsResponse,
        totalPages: 3,
        currentPage: 2,
      });

      customRender(<KafkaEnvironments />, {
        memoryRouter: true,
        queryClient: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the Kafka Environments table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: "Kafka Environments overview, page 2 of 3",
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
      mockGetPaginatedEnvironmentsForTopicAndAcl.mockResolvedValue({
        ...mockedEnvironmentsResponse,
        totalPages: 4,
        currentPage: 2,
      });

      customRender(<KafkaEnvironments />, {
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

      expect(mockGetPaginatedEnvironmentsForTopicAndAcl).toHaveBeenCalledWith({
        ...defaultApiParams,
        pageNo: "3",
      });
    });
  });

  describe("handles user searching with search input", () => {
    const testSearchInput = "My search name";

    beforeEach(async () => {
      mockGetPaginatedEnvironmentsForTopicAndAcl.mockResolvedValue(
        mockedEnvironmentsResponse
      );

      customRender(<KafkaEnvironments />, {
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
        name: "Search Kafka Environment",
      });
      expect(search).toHaveValue("");

      await userEvent.type(search, testSearchInput);

      expect(search).toHaveValue(testSearchInput);

      await waitFor(() =>
        expect(mockGetPaginatedEnvironmentsForTopicAndAcl).toHaveBeenCalledWith(
          {
            ...defaultApiParams,
            searchEnvParam: testSearchInput,
          }
        )
      );
    });

    it("enables user to navigate to search input with keyboard", async () => {
      const search = screen.getByRole("search", {
        name: "Search Kafka Environment",
      });

      expect(search).toHaveValue("");

      await tabNavigateTo({ targetElement: search });

      expect(search).toHaveFocus();
    });
  });
});
