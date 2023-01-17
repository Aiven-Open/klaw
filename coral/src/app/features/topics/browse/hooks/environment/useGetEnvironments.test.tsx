import { QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { ReactElement } from "react";
import { server } from "src/services/api-mocks/server";
import { useGetEnvironments } from "src/app/features/topics/browse/hooks/environment/useGetEnvironments";
import { mockGetEnvironments } from "src/domain/environment";
import { getQueryClientForTests } from "src/services/test-utils/query-client-tests";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";

const wrapper = ({ children }: { children: ReactElement }) => (
  <QueryClientProvider client={getQueryClientForTests()}>
    {children}
  </QueryClientProvider>
);

describe("useGetEnvironments", () => {
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
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });

      const { result } = await renderHook(() => useGetEnvironments(), {
        wrapper,
      });
      expect(result.current.isLoading).toBe(true);

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });
    });

    it("returns an error when request fails", async () => {
      console.error = jest.fn();

      mockGetEnvironments({
        mswInstance: server,
        response: { status: 400, data: { message: "error" } },
      });

      const { result } = await renderHook(() => useGetEnvironments(), {
        wrapper,
      });

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });
      expect(console.error).toHaveBeenCalledTimes(1);
    });
  });

  describe("handles successful response", () => {
    it("returns a list of environments", async () => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });

      const { result } = await renderHook(() => useGetEnvironments(), {
        wrapper,
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual([
        {
          name: "DEV",
          id: "1",
          maxPartitions: undefined,
          maxReplicationFactor: undefined,
          topicNamePrefix: undefined,
          topicNameSuffix: undefined,
          defaultPartitions: undefined,
          defaultReplicationFactor: undefined,
          type: "kafka",
        },
        {
          name: "TST",
          id: "2",
          maxPartitions: undefined,
          maxReplicationFactor: undefined,
          topicNamePrefix: undefined,
          topicNameSuffix: undefined,
          defaultPartitions: undefined,
          defaultReplicationFactor: undefined,
          type: "kafka",
        },
      ]);
    });
  });
});
