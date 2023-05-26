import { cleanup, renderHook } from "@testing-library/react";
import { BrowserRouter, MemoryRouter } from "react-router-dom";
import { useOffsetFilter } from "src/app/features/topics/overview/messages/useOffsetFilter";

describe("useOffsetFilter.tsx", () => {
  afterEach(() => {
    window.history.pushState({}, "", "/");
    cleanup();
  });
  it("returns 5 as the default offset value", () => {
    const {
      result: { current },
    } = renderHook(() => useOffsetFilter(), {
      wrapper: ({ children }) => <MemoryRouter>{children}</MemoryRouter>,
    });
    const offset = current[0];
    expect(offset).toBe("5");
  });
  it("gets the offset from search params", () => {
    const {
      result: { current },
    } = renderHook(() => useOffsetFilter(), {
      wrapper: ({ children }) => (
        <MemoryRouter initialEntries={["/?offset=25"]}>{children}</MemoryRouter>
      ),
    });
    const offset = current[0];
    expect(offset).toEqual("25");
  });
  it("gets the default offset if provided value is not one of the fixed values", () => {
    const {
      result: { current },
    } = renderHook(() => useOffsetFilter(), {
      wrapper: ({ children }) => (
        <MemoryRouter initialEntries={["/?offset=100"]}>
          {children}
        </MemoryRouter>
      ),
    });
    const offset = current[0];
    expect(offset).toEqual("5");
  });
  it("sets the offset to search params", async () => {
    const {
      result: { current },
    } = renderHook(() => useOffsetFilter(), {
      wrapper: ({ children }) => <BrowserRouter>{children}</BrowserRouter>,
    });
    const setOffset = current[1];
    setOffset("25");
    expect(window.location.search).toBe("?offset=25");
  });
});
