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
import {
  createMockTopic,
  createMockTopicApiResponse,
} from "src/domain/topic/topic-test-helper";
import { ALL_TEAMS_VALUE } from "src/domain/team/team-types";
import api from "src/services/api";

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

function getSpyCallUrls(spy: jest.SpyInstance<Promise<unknown>>): URL[] {
  return spy.mock.calls.map(([url]) => new URL(url, "http://localhost:8080"));
}

describe("useGetTopics", () => {
  const originalConsoleError = console.error;
  const spyGet = jest.spyOn(api, "get");

  beforeAll(() => {
    server.listen();
  });

  afterEach(() => {
    server.resetHandlers();
    spyGet.mockClear();
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

  describe("when called with a page number", () => {
    it("returns a list of topics with one page if api call is successful", async () => {
      mockTopicGetRequest({
        mswInstance: server,
        response: { data: mockedResponseSinglePage },
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

      const apiCallUrls = getSpyCallUrls(spyGet);
      expect(apiCallUrls.length).toBe(1);
      expect(apiCallUrls[0].pathname).toEqual("/getTopics");
      expect(apiCallUrls[0].searchParams.get("pageNo")).toEqual("1");
      expect(apiCallUrls[0].searchParams.get("env")).toEqual("ALL");
      expect(apiCallUrls[0].searchParams.get("teamName")).toEqual(null);
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

      const apiCallUrls = getSpyCallUrls(spyGet);
      expect(apiCallUrls.length).toBe(1);
      expect(apiCallUrls[0].pathname).toEqual("/getTopics");
      expect(apiCallUrls[0].searchParams.get("pageNo")).toEqual("2");
      expect(apiCallUrls[0].searchParams.get("env")).toEqual("ALL");
      expect(apiCallUrls[0].searchParams.get("teamName")).toEqual(null);
    });

    it("includes 'pageNo' as a query parameter in the API request", async () => {
      mockTopicGetRequest({
        mswInstance: server,
        response: {
          data: createMockTopicApiResponse({
            entries: 10,
            currentPage: 3,
            totalPages: 10,
          }),
        },
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

      const apiCallUrls = getSpyCallUrls(spyGet);
      expect(apiCallUrls.length).toBe(1);
      expect(apiCallUrls[0].pathname).toEqual("/getTopics");
      expect(Array.from(apiCallUrls[0].searchParams).length).toBe(2);
      expect(apiCallUrls[0].searchParams.get("pageNo")).toEqual("3");
      expect(apiCallUrls[0].searchParams.get("env")).toEqual("ALL");
      expect(apiCallUrls[0].searchParams.get("teamName")).toEqual(null);
      expect(apiCallUrls[0].searchParams.get("topicnamesearch")).toEqual(null);
    });
  });

  describe("when called with environment name", () => {
    it("includes 'env' as a query parameter in the API request", async () => {
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

      const apiCallUrls = getSpyCallUrls(spyGet);
      expect(apiCallUrls.length).toBe(1);
      expect(apiCallUrls[0].pathname).toEqual("/getTopics");
      expect(Array.from(apiCallUrls[0].searchParams).length).toBe(2);
      expect(apiCallUrls[0].searchParams.get("pageNo")).toEqual("1");
      expect(apiCallUrls[0].searchParams.get("env")).toEqual("DEV");
      expect(apiCallUrls[0].searchParams.get("teamName")).toEqual(null);
      expect(apiCallUrls[0].searchParams.get("topicnamesearch")).toEqual(null);
    });
  });

  describe("when called with team name", () => {
    it("includes team name as a query parameter in the API request", async () => {
      mockTopicGetRequest({
        mswInstance: server,
        response: { data: [] },
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

      const apiCallUrls = getSpyCallUrls(spyGet);
      expect(apiCallUrls.length).toBe(1);
      expect(apiCallUrls[0].pathname).toEqual("/getTopics");
      expect(Array.from(apiCallUrls[0].searchParams).length).toBe(3);
      expect(apiCallUrls[0].searchParams.get("pageNo")).toEqual("1");
      expect(apiCallUrls[0].searchParams.get("env")).toEqual("ALL");
      expect(apiCallUrls[0].searchParams.get("teamName")).toEqual(
        "TEST_TEAM_02"
      );
      expect(apiCallUrls[0].searchParams.get("topicnamesearch")).toEqual(null);
    });
  });

  describe("when called with searchTerm", () => {
    const searchTerm = "Searched for topic";

    it("includes search term as a query parameter in the API request", async () => {
      mockTopicGetRequest({
        mswInstance: server,
        response: { data: [] },
      });

      const spyGet = jest.spyOn(api, "get");

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

      const apiCallUrls = getSpyCallUrls(spyGet);
      expect(apiCallUrls.length).toBe(1);
      expect(apiCallUrls[0].pathname).toEqual("/getTopics");
      expect(Array.from(apiCallUrls[0].searchParams).length).toBe(3);
      expect(apiCallUrls[0].searchParams.get("pageNo")).toEqual("1");
      expect(apiCallUrls[0].searchParams.get("env")).toEqual("ALL");
      expect(apiCallUrls[0].searchParams.get("teamName")).toEqual(null);
      expect(apiCallUrls[0].searchParams.get("topicnamesearch")).toEqual(
        "Searched for topic"
      );
    });
  });
});
