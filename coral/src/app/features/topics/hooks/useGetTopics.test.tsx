import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { useGetTopics } from "src/app/features/topics/hooks/useGetTopics";
import { ReactElement } from "react";
import { server } from "src/services/api-mocks/server";
import {
  mockedResponseTransformed,
  mockTopicGetRequest,
} from "src/domain/topics/topics-api.msw";

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
  beforeAll(() => {
    server.listen();
  });

  afterEach(() => {
    server.resetHandlers();
  });

  afterAll(() => {
    server.close();
  });

  it("returns a loading state before starting to fetch data", async () => {
    mockTopicGetRequest({ mswInstance: server });

    const { result } = await renderHook(() => useGetTopics(), {
      wrapper,
    });
    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
  });

  it("returns an error when request fails", async () => {
    mockTopicGetRequest({ mswInstance: server, scenario: "error" });

    const { result } = await renderHook(() => useGetTopics(), {
      wrapper,
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(false);
    });
  });

  it("returns a list of topics if api call is successful", async () => {
    mockTopicGetRequest({ mswInstance: server });

    const { result } = await renderHook(() => useGetTopics(), {
      wrapper,
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
    expect(result.current.data).toEqual(mockedResponseTransformed);
  });
});
