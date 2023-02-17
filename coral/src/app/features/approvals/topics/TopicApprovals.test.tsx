import * as ReactQuery from "@tanstack/react-query";
import { cleanup, screen, within } from "@testing-library/react";
import { getTopicRequestsForApprover } from "src/domain/topic/topic-api";
import { transformGetTopicRequestsForApproverResponse } from "src/domain/topic/topic-transformer";
import { TopicRequestTypes, TopicRequestStatus } from "src/domain/topic";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { TopicRequestApiResponse } from "src/domain/topic/topic-types";
import TopicApprovals from "src/app/features/approvals/topics/TopicApprovals";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";

jest.mock("src/domain/topic/topic-api.ts");

const mockGetTopicRequestsForApprover =
  getTopicRequestsForApprover as jest.MockedFunction<
    typeof getTopicRequestsForApprover
  >;

const useQuerySpy = jest.spyOn(ReactQuery, "useQuery");

const mockedResponse = [
  {
    topicname: "test-topic-1",
    environment: "1",
    topicpartitions: 4,
    teamname: "NCC1701D",
    remarks: "asap",
    description: "This topic is for test",
    replicationfactor: "2",
    environmentName: "BRG",
    topicid: 1000,
    advancedTopicConfigEntries: [
      {
        configKey: "cleanup.policy",
        configValue: "delete",
      },
    ],
    topictype: "Create" as TopicRequestTypes,
    requestor: "jlpicard",
    requesttime: "1987-09-28T13:37:00.001+00:00",
    requesttimestring: "28-Sep-1987 13:37:00",
    topicstatus: "created" as TopicRequestStatus,
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
  },
  {
    topicname: "test-topic-2",
    environment: "1",
    topicpartitions: 4,
    teamname: "MIRRORUNIVERSE",
    remarks: "asap",
    description: "This topic is for test",
    replicationfactor: "2",
    environmentName: "SBY",
    topicid: 1001,
    advancedTopicConfigEntries: [
      {
        configKey: "compression.type",
        configValue: "snappy",
      },
    ],

    topictype: "Update" as TopicRequestTypes,
    requestor: "bcrusher",
    requesttime: "1994-23-05T13:37:00.001+00:00",
    requesttimestring: "23-May-1994 13:37:00",
    topicstatus: "approved" as TopicRequestStatus,
    totalNoPages: "1",
    approvingTeamDetails:
      "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf,",
    teamId: 1003,
    allPageNos: ["1"],
    currentPage: "1",
  },
];

const mockedApiResponse: TopicRequestApiResponse =
  transformGetTopicRequestsForApproverResponse(mockedResponse);

describe("TopicApprovals", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("handles loading and error state when fetching the requests", () => {
    afterEach(cleanup);
    afterAll(() => {
      useQuerySpy.mockRestore();
    });

    it("shows a loading state instead of a table while topic requests are being fetched", () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      useQuerySpy.mockReturnValue({ data: { entries: [] }, isLoading: true });

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      const table = screen.queryByRole("table");
      const loading = screen.getByTestId("skeleton-table");

      expect(table).not.toBeInTheDocument();
      expect(loading).toBeVisible();
    });

    it("shows a error message in case of an error for fetching topic requests", () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      useQuerySpy.mockReturnValue({ data: { entries: [] }, isError: true });

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      const table = screen.queryByRole("table");
      const errorMessage = screen.getByText(
        "Unexpected error. Please try again later!"
      );

      expect(table).not.toBeInTheDocument();
      expect(errorMessage).toBeVisible();
    });
  });

  describe("renders all necessary elements ", () => {
    beforeAll(async () => {
      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a select to filter by teams", () => {
      const select = screen.getByRole("combobox", { name: "Filter by team" });

      expect(select).toBeVisible();
    });

    it("shows a select to filter by environment", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by Environment",
      });

      expect(select).toBeVisible();
    });

    it("shows a select to filter by status", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by status",
      });

      expect(select).toBeVisible();
    });

    it("shows a search input to search for topic names", () => {
      const search = screen.getByRole("search");

      expect(search).toBeVisible();
      expect(search).toHaveAccessibleDescription(
        'Press "Enter" to start your search. Press "Escape" to delete all your input.'
      );
    });

    it("shows a table with all topic requests", () => {
      const table = screen.getByRole("table", { name: "Topic requests" });
      const rows = within(table).getAllByRole("rowgroup");

      expect(table).toBeVisible();
      expect(rows).toHaveLength(mockedApiResponse.entries.length);
    });
  });

  describe("renders pagination dependent on response", () => {
    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows no pagination for a response with only one page", async () => {
      mockGetTopicRequestsForApprover.mockResolvedValue(mockedApiResponse);

      customRender(<TopicApprovals />, {
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
      mockGetTopicRequestsForApprover.mockResolvedValue({
        totalPages: 2,
        currentPage: 1,
        entries: [],
      });

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
      });

      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));

      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 1 of 2",
      });
      expect(pagination).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetTopicRequestsForApprover.mockResolvedValue({
        totalPages: 3,
        currentPage: 1,
        entries: [],
      });

      customRender(<TopicApprovals />, {
        queryClient: true,
        memoryRouter: true,
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
        "Pagination navigation, you're on page 1 of 3"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 2",
      });

      await userEvent.click(pageTwoButton);

      expect(mockGetTopicRequestsForApprover).toHaveBeenNthCalledWith(2, {
        pageNumber: 2,
        requestStatus: "all",
      });
    });
  });
});
