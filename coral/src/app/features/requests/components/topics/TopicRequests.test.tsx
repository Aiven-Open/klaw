import { cleanup, screen, waitFor } from "@testing-library/react";
import { getTopicRequests } from "src/domain/topic/topic-api";
import { transformGetTopicRequestsResponse } from "src/domain/topic/topic-transformer";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { TopicRequests } from "src/app/features/requests/components/topics/TopicRequests";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";

jest.mock("src/domain/topic/topic-api.ts");

const mockGetTopicRequests = getTopicRequests as jest.MockedFunction<
  typeof getTopicRequests
>;

const mockGetTopicRequestsResponse = transformGetTopicRequestsResponse([
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
    deleteAssociatedSchema: false,
  },
]);

describe("TopicRequests", () => {
  beforeEach(() => {
    mockIntersectionObserver();
    mockGetTopicRequests.mockResolvedValue(mockGetTopicRequestsResponse);
  });

  afterEach(() => {
    cleanup();
    jest.resetAllMocks();
  });

  it("makes a request to the api to get the teams topic requests", () => {
    customRender(<TopicRequests />, {
      queryClient: true,
      memoryRouter: true,
    });
    expect(getTopicRequests).toBeCalledTimes(1);
  });

  describe("user can filter topics based on the topic name", () => {
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("populates the filter from the url search parameters", () => {
      customRender(<TopicRequests />, {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/?topic=abc",
      });
      expect(getTopicRequests).toHaveBeenNthCalledWith(1, {
        pageNo: "1",
        // search: "abc",
      });
    });

    it("applies the topic filter by typing into to the search input", async () => {
      customRender(<TopicRequests />, {
        queryClient: true,
        memoryRouter: true,
      });
      const search = screen.getByRole("search");
      expect(search).toBeVisible();
      expect(search).toHaveAccessibleDescription(
        'Search for an exact match for topic name. Searching starts automatically with a little delay while typing. Press "Escape" to delete all your input.'
      );
      await userEvent.type(search, "abc");
      await waitFor(() => {
        expect(getTopicRequests).toHaveBeenLastCalledWith({
          pageNo: "1",
          // search: "abc",
        });
      });
    });
  });
});
