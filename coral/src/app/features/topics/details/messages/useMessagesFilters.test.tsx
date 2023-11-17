import { cleanup, renderHook } from "@testing-library/react";
import { BrowserRouter, MemoryRouter } from "react-router-dom";
import { useMessagesFilters } from "src/app/features/topics/details/messages/useMessagesFilters";

describe("useMessagesFilters.tsx", () => {
  afterEach(() => {
    window.history.pushState({}, "", "/");
    cleanup();
  });
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
});
