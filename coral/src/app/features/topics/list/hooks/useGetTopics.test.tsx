import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { useGetTopics } from "src/app/features/topics/list/hooks/useGetTopics";
import { ReactElement } from "react";
import { server } from "src/services/api-mocks/server";
import {
  mockedResponseMultiplePageTransformed,
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

  it("returns a loading state before starting to fetch data", async () => {
    mockTopicGetRequest({
      mswInstance: server,
      scenario: "single-page-static",
    });

    const { result } = await renderHook(() => useGetTopics(1), {
      wrapper,
    });
    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
  });

  it("returns an error when request fails", async () => {
    console.error = jest.fn();

    mockTopicGetRequest({ mswInstance: server, scenario: "error" });

    const { result } = await renderHook(() => useGetTopics(1), {
      wrapper,
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });
    expect(console.error).toHaveBeenCalledTimes(1);
  });

  it("returns a list of topics with one page if api call is successful", async () => {
    mockTopicGetRequest({
      mswInstance: server,
      scenario: "single-page-static",
    });

    const { result } = await renderHook(() => useGetTopics(1), {
      wrapper,
    });

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

    const { result } = await renderHook(() => useGetTopics(2), {
      wrapper,
    });

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

    const { result } = await renderHook(() => useGetTopics(3), {
      wrapper,
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data?.currentPage).toBe(3);
  });
});
