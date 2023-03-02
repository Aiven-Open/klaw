import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook } from "@testing-library/react";
import { ReactElement } from "react";
import { MemoryRouter } from "react-router-dom";
import useTableFilters from "src/app/features/approvals/acls/hooks/useTableFilters";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      cacheTime: Infinity,
    },
  },
});

const wrapper = ({
  children,
  searchParam,
}: {
  children: ReactElement;
  searchParam?: string;
}) => (
  <QueryClientProvider client={queryClient}>
    <MemoryRouter initialEntries={[searchParam || ""]}>{children}</MemoryRouter>
  </QueryClientProvider>
);

describe("useTableFilters.tsx", () => {
  describe("return correct default values", () => {
    it("returns correct list of fields", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper,
      });

      expect(result.current.filters).toHaveLength(4);
    });
    it("returns 'CREATED' value by default for status state", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper,
      });

      expect(result.current.status).toBe("CREATED");
    });
    it("returns 'ALL' value by default for environment state", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper,
      });

      expect(result.current.environment).toBe("ALL");
    });
    it("returns 'ALL' value by default for aclType state", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper,
      });
      expect(result.current.aclType).toBe("ALL");
    });
    it("returns '' value by default for topic state", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper,
      });

      expect(result.current.topic).toBe("");
    });
  });

  describe("return correct default values when there are searchParams", () => {
    it("returns 'declined' value by default if there is a 'status' search param", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) =>
          wrapper({ children, searchParam: "?status=declined" }),
      });

      expect(result.current.status).toBe("declined");
    });
    it("returns '1' value by default if there is an 'env' search param", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) => wrapper({ children, searchParam: "?env=1" }),
      });

      expect(result.current.environment).toBe("1");
    });
    it("returns 'CONSUMER' value by default if there is an 'aclType' search param", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) =>
          wrapper({ children, searchParam: "?aclType=CONSUMER" }),
      });
      expect(result.current.aclType).toBe("CONSUMER");
    });
    it("returns 'topicname' value by default if there is a 'topic' search param", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) =>
          wrapper({ children, searchParam: "?topic=topicname" }),
      });
      expect(result.current.topic).toBe("topicname");
    });
    it("handles all search params at the same time", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) =>
          wrapper({
            children,
            searchParam:
              "?status=declined&env=1&aclType=CONSUMER&topic=topicname",
          }),
      });
      expect(result.current.status).toBe("declined");
      expect(result.current.environment).toBe("1");
      expect(result.current.aclType).toBe("CONSUMER");
      expect(result.current.topic).toBe("topicname");
    });
  });
});
