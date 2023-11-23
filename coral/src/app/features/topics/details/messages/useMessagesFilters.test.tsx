import { cleanup, renderHook, act } from "@testing-library/react";
import { BrowserRouter, MemoryRouter } from "react-router-dom";
import { useMessagesFilters } from "src/app/features/topics/details/messages/useMessagesFilters";

describe("useMessagesFilters.tsx", () => {
  afterEach(() => {
    window.history.pushState({}, "", "/");
    cleanup();
  });
  describe("defaultOffsetFilters", () => {
    afterEach(cleanup);
    it("returns 5 as the default offset value", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
      });
      const offset = current.defaultOffsetFilters.defaultOffset;
      expect(offset).toBe("5");
    });
    it("gets the offset from search params", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?defaultOffset=25"]}>
            {children}
          </MemoryRouter>
        ),
      });
      const offset = current.defaultOffsetFilters.defaultOffset;
      expect(offset).toEqual("25");
    });
    it("gets the default offset if provided value is not one of the fixed values", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?defaultOffset=100"]}>
            {children}
          </MemoryRouter>
        ),
      });
      const offset = current.defaultOffsetFilters.defaultOffset;
      expect(offset).toEqual("5");
    });
    it("sets the offset to search params", async () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => <BrowserRouter>{children}</BrowserRouter>,
      });
      const setDefaultOffset = current.defaultOffsetFilters.setDefaultOffset;
      setDefaultOffset("25");
      expect(window.location.search).toBe("?defaultOffset=25");
    });
    it("deletes the offset from search params", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?defaultOffset=5"]}>
            {children}
          </MemoryRouter>
        ),
      });
      const offset = current.defaultOffsetFilters.defaultOffset;
      expect(offset).toEqual("5");
      current.defaultOffsetFilters.deleteDefaultOffset();
      expect(window.location.search).toBe("");
    });
  });
  describe("customOffsetFilters", () => {
    afterEach(cleanup);
    it("returns null as the default offset value", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
      });
      const offset = current.customOffsetFilters.customOffset;
      expect(offset).toBe(null);
    });
    it("gets the offset from search params", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?customOffset=20"]}>
            {children}
          </MemoryRouter>
        ),
      });
      const offset = current.customOffsetFilters.customOffset;
      expect(offset).toEqual("20");
    });
    it("sets the offset to search params", async () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => <BrowserRouter>{children}</BrowserRouter>,
      });
      const setDefaultOffset = current.customOffsetFilters.setCustomOffset;
      setDefaultOffset("25");
      // defaultOffset is set automatically, but this URL state should never actually happen
      expect(window.location.search).toBe(
        "?defaultOffset=custom&customOffset=25"
      );
    });
    it("deletes the offset from search params", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?customOffset=100"]}>
            {children}
          </MemoryRouter>
        ),
      });
      const offset = current.customOffsetFilters.customOffset;
      expect(offset).toEqual("100");
      current.customOffsetFilters.deleteCustomOffset();
      expect(window.location.search).toBe("");
    });
  });
  describe("partitionIdFilters", () => {
    afterEach(cleanup);
    it("returns null as the default offset value", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
      });
      const offset = current.partitionIdFilters.partitionId;
      expect(offset).toBe(null);
    });
    it("gets the partition ID from search params", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?partitionId=20"]}>
            {children}
          </MemoryRouter>
        ),
      });
      const offset = current.partitionIdFilters.partitionId;
      expect(offset).toEqual("20");
    });
    it("sets the offset to search params", async () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => <BrowserRouter>{children}</BrowserRouter>,
      });
      const setDefaultOffset = current.partitionIdFilters.setPartitionId;
      setDefaultOffset("25");
      expect(window.location.search).toBe(
        "?defaultOffset=custom&partitionId=25"
      );
    });
    it("deletes the partition ID from search params", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?partitionId=1"]}>
            {children}
          </MemoryRouter>
        ),
      });
      const partitionId = current.partitionIdFilters.partitionId;
      expect(partitionId).toEqual("1");
      current.partitionIdFilters.deletePartitionId();
      expect(window.location.search).toBe("");
    });
    it("validateFilters returns true", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=custom&partitionId=1&customOffset=100",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });
      let isValid;

      act(() => {
        isValid = result.current.validateFilters();
      });

      expect(isValid).toBe(true);
      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: null,
      });
    });
    it("validateFilters returns false (missing partitionId)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={["/?defaultOffset=custom&customOffset=100"]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters();
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: "Please enter a partition ID",
      });
    });

    it("validateFilters returns false (missing customOffset)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={["/?defaultOffset=custom&partitionId=100"]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters();
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters:
          "Please enter the number of recent offsets you want to view",
        partitionIdFilters: null,
      });
    });
    it("validateFilters returns false (too high customOffset)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=custom&customOffset=9999&partitionId=100",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters();
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters:
          "Entered value exceeds the view limit for offsets: 100",
        partitionIdFilters: null,
      });
    });
    it("getFetchingMode returns Custom", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=custom&customOffset=9999&partitionId=100",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });
      expect(current.getFetchingMode()).toBe("Custom");
    });
    it("getFetchingMode returns Default", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter initialEntries={["/?defaultOffset=5"]}>
            {children}
          </MemoryRouter>
        ),
      });
      expect(current.getFetchingMode()).toBe("Default");
    });
  });
});
