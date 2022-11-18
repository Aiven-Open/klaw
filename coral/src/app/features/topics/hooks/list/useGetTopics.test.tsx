import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { ReactElement } from "react";
import { server } from "src/services/api-mocks/server";
import {
  mockedResponseMultiplePageTransformed,
  mockedResponseSinglePage,
  mockedResponseTransformed,
  mockTopicGetRequest,
} from "src/domain/topic/topic-api.msw";

import { useGetTopics } from "src/app/features/topics/hooks/list/useGetTopics";
import { createMockTopic } from "src/domain/topic/topic-test-helper";

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

  describe.only("handles loading and error state", () => {
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

      mockTopicGetRequest({ mswInstance: server, scenario: "error" });

      const { result } = await renderHook(
        () => useGetTopics({ currentPage: 1, environment: "ALL" }),
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
        () => useGetTopics({ currentPage: 1, environment: "ALL" }),
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
        scenario: "multiple-pages-static",
      });

      const { result } = await renderHook(
        () => useGetTopics({ currentPage: 2, environment: "ALL" }),
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
        () => useGetTopics({ currentPage: 3, environment: "ALL" }),
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
      mockTopicGetRequest({
        mswInstance: server,
        scenario: "single-page-env-dev",
      });

      const { result } = await renderHook(
        () => useGetTopics({ currentPage: 1, environment: "DEV" }),
        {
          wrapper,
        }
      );

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      const envList = result.current.data?.entries
        .map((topic) => topic.environmentsList)
        .flat();

      expect(envList).toEqual(["DEV", "DEV", "DEV"]);
    });
  });
});
