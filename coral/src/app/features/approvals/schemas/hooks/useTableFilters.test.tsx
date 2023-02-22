import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook } from "@testing-library/react";
import { ReactElement } from "react";
import { MemoryRouter } from "react-router-dom";
import useTableFilters from "src/app/features/approvals/schemas/hooks/useTableFilters";

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

      expect(result.current.filters).toHaveLength(3);
    });

    it("returns 'created' value by default for environment state", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper,
      });

      expect(result.current.environment).toBe("ALL");
    });

    it("returns 'ALL' value by default for status state", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper,
      });
      expect(result.current.status).toBe("CREATED");
    });

    it("returns '' value by default for topic state", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper,
      });

      expect(result.current.topic).toBe("");
    });
  });

  describe("return correct values when there are searchParams", () => {
    it("returns '1' if 'env' search param is set to 1", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) => wrapper({ children, searchParam: "?env=1" }),
      });

      expect(result.current.environment).toBe("1");
    });

    it("returns value DECLINED if 'status' search param is set to DECLINED", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) =>
          wrapper({ children, searchParam: "?status=DECLINED" }),
      });

      expect(result.current.status).toBe("DECLINED");
    });

    it("returns value my-topic if 'topic' search param is set to my-topic", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) =>
          wrapper({ children, searchParam: "?topic=my-topic" }),
      });

      expect(result.current.topic).toBe("my-topic");
    });

    it("handles all search params at the same time", () => {
      const { result } = renderHook(() => useTableFilters(), {
        wrapper: ({ children }) =>
          wrapper({
            children,
            searchParam: "?status=APPROVED&env=2&topic=myothertopic",
          }),
      });
      expect(result.current.status).toBe("APPROVED");
      expect(result.current.environment).toBe("2");
      expect(result.current.topic).toBe("myothertopic");
    });
  });
});
