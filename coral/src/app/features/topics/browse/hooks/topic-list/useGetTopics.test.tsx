import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { ReactElement } from "react";
import { server } from "src/services/api-mocks/server";
import {
  mockedResponseMultiplePage,
  mockedResponseMultiplePageTransformed,
  mockedResponseSinglePage,
  mockedResponseTopicEnv,
  mockedResponseTransformed,
  mockTopicGetRequest,
} from "src/domain/topic/topic-api.msw";

import { useGetTopics } from "src/app/features/topics/browse/hooks/topic-list/useGetTopics";
import { createMockTopic } from "src/domain/topic/topic-test-helper";
import { ALL_TEAMS_VALUE } from "src/domain/team/team-types";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      cacheTime: Infinity,
    },
  },
});

const wrapper = ({ children }: { children: ReactElement }) => (
  <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
);

describe("useGetTopics", () => {
  const originalConsoleError = console.error;

  beforeAll(() => {
    server.listen();
  });

  afterEach(() => {
    server.resetHandlers();
  });

  afterAll(() => {
    console.error = originalConsoleError;
    server.close();
  });

  describe("handles loading and error state", () => {
    it("returns a loading state before starting to fetch data", async () => {
      const responseData = [
        [
          createMockTopic({
            topicName: "Topic 1",
            topicId: 1,
            environmentsList: ["DEV"],
          }),
        ],
      ];
      mockTopicGetRequest({
        mswInstance: server,
        response: {
          status: 200,
          data: responseData,
        },
      });

      const { result } = await renderHook(
        () =>
          useGetTopics({
            currentPage: 1,
            environment: "ALL",
            teamName: ALL_TEAMS_VALUE,
          }),
        {
          wrapper,
        }
      );
      expect(result.current.isLoading).toBe(true);

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });
    });

    it("returns an error when request fails", async () => {
      console.error = jest.fn();

      mockTopicGetRequest({
        mswInstance: server,
        response: { status: 400, data: { message: "Not relevant" } },
      });

      const { result } = await renderHook(
        () =>
          useGetTopics({
            currentPage: 1,
            environment: "ALL",
            teamName: ALL_TEAMS_VALUE,
          }),
        {
          wrapper,
        }
      );

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });
      expect(console.error).toHaveBeenCalledTimes(1);
    });
  });

  describe("handles paginated responses", () => {
    it("returns a list of topics with one page if api call is successful", async () => {
      mockTopicGetRequest({
        mswInstance: server,
        response: { status: 200, data: mockedResponseSinglePage },
      });

      const { result } = await renderHook(
        () =>
          useGetTopics({
            currentPage: 1,
            environment: "ALL",
            teamName: ALL_TEAMS_VALUE,
          }),
        {
          wrapper,
        }
      );

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(mockedResponseTransformed);
    });

    it("returns a list of topics with 2 pages if api call is successful", async () => {
      mockTopicGetRequest({
        mswInstance: server,
        response: { data: mockedResponseMultiplePage },
      });

      const { result } = await renderHook(
        () =>
          useGetTopics({
            currentPage: 2,
            environment: "ALL",
            teamName: ALL_TEAMS_VALUE,
          }),
        {
          wrapper,
        }
      );

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toMatchObject(
        mockedResponseMultiplePageTransformed
      );
    });

    it("returns a list of topics with current page set to 3", async () => {
      mockTopicGetRequest({
        mswInstance: server,
      });

      const { result } = await renderHook(
        () =>
          useGetTopics({
            currentPage: 3,
            environment: "ALL",
            teamName: ALL_TEAMS_VALUE,
          }),
        {
          wrapper,
        }
      );

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.currentPage).toBe(3);
    });
  });

  describe("handles responses based on the environment", () => {
    it("returns a list of three topics with `DEV` envs", async () => {
      const expectedFilterByEnvResult = [
        {
          allPageNos: ["1"],
          cluster: "1",
          clusterId: null,
          currentPage: "1",
          description: "Topic description",
          documentation: null,
          environmentsList: ["DEV"],
          noOfPartitions: 2,
          noOfReplcias: "2",
          sequence: "341",
          showDeleteTopic: false,
          showEditTopic: false,
          teamname: "DevRel",
          topicDeletable: false,
          topicName: "Topic 1",
          topicid: 1,
          totalNoPages: "1",
        },
        {
          allPageNos: ["1"],
          cluster: "1",
          clusterId: null,
          currentPage: "1",
          description: "Topic description",
          documentation: null,
          environmentsList: ["DEV"],
          noOfPartitions: 2,
          noOfReplcias: "2",
          sequence: "341",
          showDeleteTopic: false,
          showEditTopic: false,
          teamname: "DevRel",
          topicDeletable: false,
          topicName: "Topic 2",
          topicid: 2,
          totalNoPages: "1",
        },
        {
          allPageNos: ["1"],
          cluster: "1",
          clusterId: null,
          currentPage: "1",
          description: "Topic description",
          documentation: null,
          environmentsList: ["DEV"],
          noOfPartitions: 2,
          noOfReplcias: "2",
          sequence: "341",
          showDeleteTopic: false,
          showEditTopic: false,
          teamname: "DevRel",
          topicDeletable: false,
          topicName: "Topic 3",
          topicid: 3,
          totalNoPages: "1",
        },
      ];
      mockTopicGetRequest({
        mswInstance: server,
        response: { data: mockedResponseTopicEnv },
      });

      const { result } = await renderHook(
        () =>
          useGetTopics({
            currentPage: 1,
            environment: "DEV",
            teamName: ALL_TEAMS_VALUE,
          }),
        {
          wrapper,
        }
      );

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.entries).toEqual(expectedFilterByEnvResult);
    });
  });

  describe("handles responses based on the team", () => {
    it("returns a list of two topics with `TEST_TEAM_02` team", async () => {
      const expectedFilterByTeamResult = [
        {
          allPageNos: ["1"],
          cluster: "1",
          clusterId: null,
          currentPage: "1",
          description: "Topic description",
          documentation: null,
          environmentsList: ["DEV", "TEST"],
          noOfPartitions: 2,
          noOfReplcias: "2",
          sequence: "341",
          showDeleteTopic: false,
          showEditTopic: false,
          teamname: "TEST_TEAM_02",
          topicDeletable: false,
          topicName: "Topic 1",
          topicid: 1,
          totalNoPages: "1",
        },
        {
          allPageNos: ["1"],
          cluster: "1",
          clusterId: null,
          currentPage: "1",
          description: "Topic description",
          documentation: null,
          environmentsList: ["DEV", "TEST"],
          noOfPartitions: 2,
          noOfReplcias: "2",
          sequence: "341",
          showDeleteTopic: false,
          showEditTopic: false,
          teamname: "TEST_TEAM_02",
          topicDeletable: false,
          topicName: "Topic 2",
          topicid: 2,
          totalNoPages: "1",
        },
      ];
      mockTopicGetRequest({
        mswInstance: server,
      });

      const { result } = await renderHook(
        () =>
          useGetTopics({
            currentPage: 1,
            environment: "ALL",
            teamName: "TEST_TEAM_02",
          }),
        {
          wrapper,
        }
      );

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.entries).toEqual(expectedFilterByTeamResult);
    });
  });

  describe("handles responses based on a search term", () => {
    const searchTerm = "Searched for topic";

    it(`returns a list of two topics matching a "${searchTerm}" search`, async () => {
      const expectedSearchResult = [
        {
          allPageNos: ["1"],
          cluster: "1",
          clusterId: null,
          currentPage: "1",
          description: "Topic description",
          documentation: null,
          environmentsList: ["DEV", "TEST"],
          noOfPartitions: 2,
          noOfReplcias: "2",
          sequence: "341",
          showDeleteTopic: false,
          showEditTopic: false,
          teamname: "TEST_TEAM_01",
          topicDeletable: false,
          topicName: "Searched for topic 1",
          topicid: 1,
          totalNoPages: "1",
        },
        {
          allPageNos: ["1"],
          cluster: "1",
          clusterId: null,
          currentPage: "1",
          description: "Topic description",
          documentation: null,
          environmentsList: ["DEV", "TEST"],
          noOfPartitions: 2,
          noOfReplcias: "2",
          sequence: "341",
          showDeleteTopic: false,
          showEditTopic: false,
          teamname: "TEST_TEAM_02",
          topicDeletable: false,
          topicName: "Searched for topic 2",
          topicid: 2,
          totalNoPages: "1",
        },
      ];
      mockTopicGetRequest({
        mswInstance: server,
      });

      const { result } = await renderHook(
        () =>
          useGetTopics({
            currentPage: 1,
            environment: "ALL",
            teamName: ALL_TEAMS_VALUE,
            searchTerm,
          }),
        {
          wrapper,
        }
      );

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.entries).toEqual(expectedSearchResult);
    });
  });
});
