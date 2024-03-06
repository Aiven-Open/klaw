import { cleanup, renderHook } from "@testing-library/react";
import { BrowserRouter, MemoryRouter } from "react-router-dom";
import {
  FiltersProvider,
  useFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";

describe("useFiltersValues.tsx", () => {
  describe("gets correct filter values from search params", () => {
    afterEach(() => {
      cleanup();
    });

    it("gets the correct environment filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?environment=1"]}>
            <FiltersProvider>{children}</FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.environment).toBe("1");
    });

    it("gets the correct aclType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?aclType=PRODUCER"]}>
            <FiltersProvider>{children}</FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.aclType).toBe("PRODUCER");
    });

    it("gets the correct status filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?status=CREATED"]}>
            <FiltersProvider>{children}</FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.status).toBe("CREATED");
    });

    it("gets the correct team filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?teamId=1"]}>
            <FiltersProvider>{children}</FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.teamId).toBe("1");
    });

    it("gets the correct showOnlyMyRequests filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?showOnlyMyRequests=true"]}>
            <FiltersProvider>{children}</FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.showOnlyMyRequests).toBe(true);
    });

    it("gets the correct operationType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?requestType=CLAIM"]}>
            <FiltersProvider>{children}</FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.requestType).toBe("CLAIM");
    });

    it("gets the correct search filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?search=abc"]}>
            <FiltersProvider>{children}</FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.search).toBe("abc");
    });

    it("gets the correct clusterType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?clusterType=KAFKA"]}>
            <FiltersProvider>{children}</FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.clusterType).toBe("KAFKA");
    });
  });

  describe("gets correct filter values when default value is passed", () => {
    afterEach(() => {
      cleanup();
    });

    it("gets the correct environment filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter>
            <FiltersProvider defaultValues={{ environment: "2" }}>
              {children}
            </FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.environment).toBe("2");
    });

    it("gets the correct aclType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter>
            <FiltersProvider defaultValues={{ aclType: "CONSUMER" }}>
              {children}
            </FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.aclType).toBe("CONSUMER");
    });

    it("gets the correct status filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter>
            <FiltersProvider defaultValues={{ status: "DELETED" }}>
              {children}
            </FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.status).toBe("DELETED");
    });

    it("gets the correct team filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter>
            <FiltersProvider defaultValues={{ teamId: "2" }}>
              {children}
            </FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.teamId).toBe("2");
    });

    it("gets the correct showOnlyMyRequests filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter>
            <FiltersProvider defaultValues={{ showOnlyMyRequests: false }}>
              {children}
            </FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.showOnlyMyRequests).toBe(false);
    });

    it("gets the correct operationType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter>
            <FiltersProvider defaultValues={{ requestType: "CREATE" }}>
              {children}
            </FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.requestType).toBe("CREATE");
    });

    it("gets the correct search filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter>
            <FiltersProvider defaultValues={{ search: "abc" }}>
              {children}
            </FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.search).toBe("abc");
    });

    it("gets the correct clusterType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <MemoryRouter>
            <FiltersProvider defaultValues={{ clusterType: "SCHEMA_REGISTRY" }}>
              {children}
            </FiltersProvider>
          </MemoryRouter>
        ),
      });

      expect(current.clusterType).toBe("SCHEMA_REGISTRY");
    });
  });

  describe("should set correct filter values when using setFilterValue", () => {
    afterEach(() => {
      window.history.pushState({}, "", "/");
      cleanup();
    });

    it("sets the correct environment filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <BrowserRouter>
            <FiltersProvider>{children}</FiltersProvider>
          </BrowserRouter>
        ),
      });

      current.setFilterValue({
        name: "environment",
        value: "1",
      });

      expect(window.location.search).toBe("?environment=1&page=1");
    });

    it("sets the correct aclType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <BrowserRouter>
            <FiltersProvider>{children}</FiltersProvider>
          </BrowserRouter>
        ),
      });

      current.setFilterValue({
        name: "aclType",
        value: "PRODUCER",
      });

      expect(window.location.search).toBe("?aclType=PRODUCER&page=1");
    });

    it("sets the correct status filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <BrowserRouter>
            <FiltersProvider>{children}</FiltersProvider>
          </BrowserRouter>
        ),
      });

      current.setFilterValue({
        name: "status",
        value: "CREATED",
      });

      expect(window.location.search).toBe("?status=CREATED&page=1");
    });

    it("sets the correct team filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <BrowserRouter>
            <FiltersProvider>{children}</FiltersProvider>
          </BrowserRouter>
        ),
      });

      current.setFilterValue({
        name: "teamId",
        value: "2",
      });

      expect(window.location.search).toBe("?teamId=2&page=1");
    });

    it("sets the correct showOnlyMyRequests filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <BrowserRouter>
            <FiltersProvider>{children}</FiltersProvider>
          </BrowserRouter>
        ),
      });

      current.setFilterValue({
        name: "showOnlyMyRequests",
        value: true,
      });

      expect(window.location.search).toBe("?showOnlyMyRequests=true&page=1");
    });

    it("sets the correct operationType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <BrowserRouter>
            <FiltersProvider>{children}</FiltersProvider>
          </BrowserRouter>
        ),
      });

      current.setFilterValue({
        name: "requestType",
        value: "CREATE",
      });

      expect(window.location.search).toBe("?requestType=CREATE&page=1");
    });

    it("sets the correct search filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <BrowserRouter>
            <FiltersProvider>{children}</FiltersProvider>
          </BrowserRouter>
        ),
      });

      current.setFilterValue({
        name: "search",
        value: "abc",
      });

      expect(window.location.search).toBe("?search=abc&page=1");
    });

    it("sets the correct clusterType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersContext(), {
        wrapper: ({ children }) => (
          <BrowserRouter>
            <FiltersProvider>{children}</FiltersProvider>
          </BrowserRouter>
        ),
      });

      current.setFilterValue({
        name: "clusterType",
        value: "KAFKA_CONNECT",
      });

      expect(window.location.search).toBe("?clusterType=KAFKA_CONNECT&page=1");
    });
  });
});
