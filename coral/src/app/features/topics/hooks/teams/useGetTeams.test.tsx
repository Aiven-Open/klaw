import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { ReactElement } from "react";
import { server } from "src/services/api-mocks/server";
import { useGetTeams } from "src/app/features/topics/hooks/teams/useGetTeams";
import { mockGetTeams } from "src/domain/team/team-api.msw";

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

describe("useGetTeams", () => {
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
      mockGetTeams({
        mswInstance: server,
      });

      const { result } = await renderHook(() => useGetTeams(), {
        wrapper,
      });
      expect(result.current.isLoading).toBe(true);

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });
    });

    it("returns an error when request fails", async () => {
      console.error = jest.fn();

      mockGetTeams({ mswInstance: server, scenario: "error" });

      const { result } = await renderHook(() => useGetTeams(), {
        wrapper,
      });

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });
      expect(console.error).toHaveBeenCalledTimes(1);
    });
  });

  describe("handles successful response", () => {
    it("returns a list of teams", async () => {
      mockGetTeams({
        mswInstance: server,
      });

      const { result } = await renderHook(() => useGetTeams(), {
        wrapper,
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual([
        "TEST_TEAM_01",
        "TEST_TEAM_02",
        "TEST_TEAM_03",
      ]);
    });
  });
});
