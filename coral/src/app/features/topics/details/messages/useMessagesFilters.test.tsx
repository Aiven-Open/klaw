import { act, cleanup, renderHook } from "@testing-library/react";
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
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(true);
      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: null,
        rangeOffsetStartFilters: null,
        rangeOffsetEndFilters: null,
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
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: "Please enter a partition ID",
        rangeOffsetStartFilters: null,
        rangeOffsetEndFilters: null,
      });
    });
    it("validateFilters returns false (negative partitionId)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=custom&customOffset=100&partitionId=-1",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: "Partition ID cannot be negative",
        rangeOffsetStartFilters: null,
        rangeOffsetEndFilters: null,
      });
    });
    it("validateFilters returns false (invalid partitionId)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=custom&customOffset=100&partitionId=6",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: "Invalid partition ID",
        rangeOffsetStartFilters: null,
        rangeOffsetEndFilters: null,
      });
    });
    it("validateFilters returns false (missing customOffset)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={["/?defaultOffset=custom&partitionId=1"]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters:
          "Please enter the number of recent offsets you want to view",
        partitionIdFilters: null,
        rangeOffsetStartFilters: null,
        rangeOffsetEndFilters: null,
      });
    });
    it("validateFilters returns false (too high customOffset)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=custom&customOffset=9999&partitionId=1",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters:
          "Entered value exceeds the view limit for offsets: 100",
        partitionIdFilters: null,
        rangeOffsetStartFilters: null,
        rangeOffsetEndFilters: null,
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
      expect(current.getFetchingMode()).toBe("custom");
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
      expect(current.getFetchingMode()).toBe("default");
    });
    it("partitionId not deleted when changing from custom to range", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=custom&customOffset=10&partitionId=2",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });
      expect(current.getFetchingMode()).toBe("custom");
      expect(current.partitionIdFilters.partitionId).toBe("2");

      current.defaultOffsetFilters.setDefaultOffset("range");
      expect(current.getFetchingMode()).toBe("range");
      expect(current.partitionIdFilters.partitionId).toBe("2");
    });
    it("partitionId not deleted when changing from range to custom", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=range&rangeOffsetStart=5&rangeOffsetEnd=10&partitionId=2",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });
      expect(current.getFetchingMode()).toBe("range");
      expect(current.partitionIdFilters.partitionId).toBe("2");

      current.defaultOffsetFilters.setDefaultOffset("custom");
      expect(current.getFetchingMode()).toBe("custom");
      expect(current.partitionIdFilters.partitionId).toBe("2");
    });
  });

  describe("rangeOffsetFilters", () => {
    afterEach(cleanup);
    it("returns null as the default offset value", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
      });

      expect(current.rangeOffsetFilters.rangeOffsetStart).toBe(null);
      expect(current.rangeOffsetFilters.rangeOffsetEnd).toBe(null);
    });
    it("gets the start and end offset from search params", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={["/?rangeOffsetStart=20&rangeOffsetEnd=40"]}
          >
            {children}
          </MemoryRouter>
        ),
      });
      const startOffset = current.rangeOffsetFilters.rangeOffsetStart;
      const endOffset = current.rangeOffsetFilters.rangeOffsetEnd;
      expect(startOffset).toEqual("20");
      expect(endOffset).toEqual("40");
    });
    it("sets the offset to search params", async () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => <BrowserRouter>{children}</BrowserRouter>,
      });
      const setRangeOffsetStart =
        current.rangeOffsetFilters.setRangeOffsetStart;
      const setRangeOffsetEnd = current.rangeOffsetFilters.setRangeOffsetEnd;
      setRangeOffsetStart("25");
      setRangeOffsetEnd("50");
      expect(window.location.search).toBe(
        "?defaultOffset=range&rangeOffsetStart=25&rangeOffsetEnd=50"
      );
    });
    it("deletes the offset from search params", () => {
      const {
        result: { current },
      } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={["/?rangeOffsetStart=25&rangeOffsetEnd=50"]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      expect(current.rangeOffsetFilters.rangeOffsetStart).toEqual("25");
      expect(current.rangeOffsetFilters.rangeOffsetEnd).toEqual("50");
      current.rangeOffsetFilters.deleteRangeOffsetStart();
      current.rangeOffsetFilters.deleteRangeOffsetEnd();
      expect(window.location.search).toBe("");
    });
    it("validateFilters returns true", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=range&partitionId=1&rangeOffsetStart=10&rangeOffsetEnd=20",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });
      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(true);
      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: null,
        rangeOffsetStartFilters: null,
        rangeOffsetEndFilters: null,
      });
    });
    it("validateFilters returns false (missing partitionId)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=range&rangeOffsetStart=10&rangeOffsetEnd=20",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: "Please enter a partition ID",
        rangeOffsetStartFilters: null,
        rangeOffsetEndFilters: null,
      });
    });
    it("validateFilters returns false (missing rangeOffsetStart and rangeOffsetEnd)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={["/?defaultOffset=range&partitionId=1"]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: null,
        rangeOffsetStartFilters: "Please enter the start offset",
        rangeOffsetEndFilters: "Please enter the end offset",
      });
    });
    it("validateFilters returns false (negative rangeOffsetStart and rangeOffsetEnd)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=range&partitionId=1&rangeOffsetStart=-10&rangeOffsetEnd=-20",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: null,
        rangeOffsetStartFilters: "Start offset cannot be negative.",
        rangeOffsetEndFilters: "End offset cannot be negative.",
      });
    });
    it("validateFilters returns false (rangeOffsetStart bigger than rangeOffsetEnd)", () => {
      const { result } = renderHook(() => useMessagesFilters(), {
        wrapper: ({ children }) => (
          <MemoryRouter
            initialEntries={[
              "/?defaultOffset=range&partitionId=1&rangeOffsetStart=100&rangeOffsetEnd=20",
            ]}
          >
            {children}
          </MemoryRouter>
        ),
      });

      let isValid;

      act(() => {
        isValid = result.current.validateFilters(5);
      });

      expect(isValid).toBe(false);

      expect(result.current.filterErrors).toStrictEqual({
        customOffsetFilters: null,
        partitionIdFilters: null,
        rangeOffsetStartFilters: "Start must me less than end.",
        rangeOffsetEndFilters: null,
      });
    });
  });
});
