import { cleanup, renderHook } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";

describe("useFiltersValues.tsx", () => {
  afterEach(() => {
    cleanup();
  });

  describe("should get correct filter values from search paramns", () => {
    it("gets the correct topic filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersValues(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?topic=topictest"]}>
            {children}
          </MemoryRouter>
        ),
      });

      expect(current.topic).toBe("topictest");
    });
    it("gets the correct environment filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersValues(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?environment=1"]}>
            {children}
          </MemoryRouter>
        ),
      });

      expect(current.environment).toBe("1");
    });
    it("gets the correct aclType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersValues(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?aclType=PRODUCER"]}>
            {children}
          </MemoryRouter>
        ),
      });

      expect(current.aclType).toBe("PRODUCER");
    });
    it("gets the correct status filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersValues(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?status=CREATED"]}>
            {children}
          </MemoryRouter>
        ),
      });

      expect(current.status).toBe("CREATED");
    });
    it("gets the correct team filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersValues(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?team=1"]}>{children}</MemoryRouter>
        ),
      });

      expect(current.team).toBe("1");
    });
    it("gets the correct showOnlyMyRequests filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersValues(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?showOnlyMyRequests=true"]}>
            {children}
          </MemoryRouter>
        ),
      });

      expect(current.showOnlyMyRequests).toBe(true);
    });
    it("gets the correct operationType filter value", () => {
      const {
        result: { current },
      } = renderHook(() => useFiltersValues(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?operationType=CLAIM"]}>
            {children}
          </MemoryRouter>
        ),
      });

      expect(current.operationType).toBe("CLAIM");
    });

    describe("should get correct filter values when default value is passed", () => {
      it("gets the correct topic filter value", () => {
        const {
          result: { current },
        } = renderHook(
          () => useFiltersValues({ defaultTopic: "defaultTest" }),
          {
            wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
          }
        );

        expect(current.topic).toBe("defaultTest");
      });
      it("gets the correct environment filter value", () => {
        const {
          result: { current },
        } = renderHook(() => useFiltersValues({ defaultEnvironment: "2" }), {
          wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
        });

        expect(current.environment).toBe("2");
      });
      it("gets the correct aclType filter value", () => {
        const {
          result: { current },
        } = renderHook(() => useFiltersValues({ defaultAclType: "CONSUMER" }), {
          wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
        });

        expect(current.aclType).toBe("CONSUMER");
      });
      it("gets the correct status filter value", () => {
        const {
          result: { current },
        } = renderHook(() => useFiltersValues({ defaultStatus: "DELETED" }), {
          wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
        });

        expect(current.status).toBe("DELETED");
      });
      it("gets the correct team filter value", () => {
        const {
          result: { current },
        } = renderHook(() => useFiltersValues({ defaultTeam: "2" }), {
          wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
        });

        expect(current.team).toBe("2");
      });
      it("gets the correct showOnlyMyRequests filter value", () => {
        const {
          result: { current },
        } = renderHook(
          () => useFiltersValues({ defaultShowOnlyMyRequests: false }),
          {
            wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
          }
        );

        expect(current.showOnlyMyRequests).toBe(false);
      });
      it("gets the correct operationType filter value", () => {
        const {
          result: { current },
        } = renderHook(
          () => useFiltersValues({ defaultOperationType: "CREATE" }),
          {
            wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
          }
        );

        expect(current.operationType).toBe("CREATE");
      });
    });
  });
});
