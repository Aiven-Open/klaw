import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import ActivityLog from "src/app/features/activity-log/ActivityLog";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-test-helper";
import { getActivityLog } from "src/domain/requests/requests-api";
import { activityLogTransformer } from "src/domain/requests/requests-transformers";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/requests/requests-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetEnvironmentRequest =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockGetActivityLog = getActivityLog as jest.MockedFunction<
  typeof getActivityLog
>;

const mockGetActivityLogResponse = activityLogTransformer([
  {
    req_no: 1001,
    tenantId: 101,
    activityName: "TopicRequest",
    activityType: "Create",
    activityTime: "2023-06-17T13:52:14.646+00:00",
    activityTimeString: "17-Jun-2023 13:52:14",
    details: "testtopic12345",
    user: "muralibasani",
    teamId: 1005,
    env: "1",
    envName: "DEV",
    team: "Ospo",
    totalNoPages: "1",
    currentPage: "1",
    allPageNos: ["1", ">", ">>"],
  },
  {
    req_no: 1002,
    tenantId: 101,
    activityName: "TopicRequest",
    activityType: "Create",
    activityTime: "2023-06-17T14:12:11.625+00:00",
    activityTimeString: "17-Jun-2023 14:12:11",
    details: "test4333",
    user: "muralibasani",
    teamId: 1005,
    env: "1",
    envName: "TST",
    team: "Ospo",
    totalNoPages: "1",
    currentPage: "1",
    allPageNos: ["1", ">", ">>"],
  },
]);

describe("ActivityLog", () => {
  beforeEach(() => {
    mockIntersectionObserver();
    mockGetActivityLog.mockResolvedValue(mockGetActivityLogResponse);
  });

  afterEach(() => {
    cleanup();
    jest.resetAllMocks();
  });

  it("makes a request to the api to get the teams connector requests", () => {
    customRender(<ActivityLog />, {
      queryClient: true,
      memoryRouter: true,
    });
    expect(mockGetActivityLog).toHaveBeenCalledTimes(1);
  });

  describe("handles loading and error state when fetching the requests", () => {
    const originalConsoleError = console.error;
    beforeEach(() => {
      // used to swallow a console.error that _should_ happen
      // while making sure to not swallow other console.errors
      console.error = jest.fn();

      mockGetEnvironmentRequest.mockResolvedValue(mockedEnvironmentResponse);
      mockGetActivityLog.mockResolvedValue({
        entries: [],
        totalPages: 1,
        currentPage: 1,
      });
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a loading state instead of a table while connector requests are being fetched", () => {
      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
      });

      const table = screen.queryByRole("table");
      const loading = screen.getByTestId("skeleton-table");

      expect(table).not.toBeInTheDocument();
      expect(loading).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("shows a error message in case of an error for fetching connector requests", async () => {
      mockGetActivityLog.mockRejectedValue("mock-error");

      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
      });

      const table = screen.queryByRole("table");
      const errorMessage = await screen.findByText(
        "Unexpected error. Please try again later!"
      );

      expect(table).not.toBeInTheDocument();
      expect(errorMessage).toBeVisible();
      expect(console.error).toHaveBeenCalledWith("mock-error");
    });
  });

  describe("user can browse the requests in paged sets", () => {
    beforeEach(() => {
      mockGetEnvironmentRequest.mockResolvedValue(mockedEnvironmentResponse);
      mockGetActivityLog.mockResolvedValue({
        totalPages: 1,
        currentPage: 1,
        entries: [],
      });
    });
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("fetches the right page number if a page is set in search params", async () => {
      const routePath = "/?page=100";
      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: routePath,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetActivityLog).toHaveBeenCalledWith({
        pageNo: "100",
      });
    });

    it("fetches the first page if no search param is defined", async () => {
      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      expect(mockGetActivityLog).toHaveBeenCalledWith({
        pageNo: "1",
      });
    });

    it("shows no pagination for a response with only one page", async () => {
      mockGetActivityLog.mockResolvedValue({
        ...mockGetActivityLogResponse,
        totalPages: 1,
      });

      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });
      expect(pagination).not.toBeInTheDocument();
    });

    it("shows a pagination when response has more then one page", async () => {
      mockGetActivityLog.mockResolvedValue({
        totalPages: 2,
        currentPage: 1,
        entries: [],
      });

      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 1 of 2",
      });
      expect(pagination).toBeVisible();
    });

    it("shows the currently active page based on api response", async () => {
      mockGetActivityLog.mockResolvedValue({
        totalPages: 4,
        currentPage: 2,
        entries: [],
      });

      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 2 of 4",
      });
      expect(pagination).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetEnvironmentRequest.mockResolvedValue(mockedEnvironmentResponse);
      mockGetActivityLog.mockResolvedValue({
        totalPages: 3,
        currentPage: 1,
        entries: [],
      });

      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 1 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 1 of 3"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 2",
      });

      await userEvent.click(pageTwoButton);

      expect(mockGetActivityLog).toHaveBeenNthCalledWith(2, {
        pageNo: "2",
      });
    });
  });

  describe("user can filter connector requests by 'environment'", () => {
    beforeEach(async () => {
      mockGetEnvironmentRequest.mockResolvedValue(mockedEnvironmentResponse);
      mockGetActivityLog.mockResolvedValue(mockGetActivityLogResponse);
      customRender(<ActivityLog />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath:
          "/?environment=TEST_ENV_THAT_CANNOT_BE_PART_OF_ANY_API_MOCK",
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("populates the filter from the url search parameters", () => {
      expect(mockGetActivityLog).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        env: "TEST_ENV_THAT_CANNOT_BE_PART_OF_ANY_API_MOCK",
      });
    });

    it("enables user to filter by 'environment'", async () => {
      const environmentFilter = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });

      const environmentOption = screen.getByRole("option", {
        name: mockedEnvironmentResponse[0].name,
      });
      await userEvent.selectOptions(environmentFilter, environmentOption);

      expect(mockGetActivityLog).toHaveBeenNthCalledWith(2, {
        pageNo: "1",
        env: mockedEnvironmentResponse[0].id,
      });
    });
  });
});
