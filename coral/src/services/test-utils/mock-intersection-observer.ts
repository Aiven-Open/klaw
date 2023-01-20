// This is needed for Design System <Table />
// I followed this approach: https://stackoverflow.com/a/58651649
// we used jest.fn() here before, which lead to errors (that sometimes
// didn't show up in the async tests) because they may get affected
// by e.g. jest.restoreAllMocks etc. in afterX methods
function mockIntersectionObserver({
  root = null,
  rootMargin = "",
  thresholds = [],
  disconnect = () => null,
  observe = () => null,
  takeRecords = () => [],
  unobserve = () => null,
} = {}): void {
  class MockIntersectionObserver implements IntersectionObserver {
    readonly root: Element | null = root;
    readonly rootMargin: string = rootMargin;
    readonly thresholds: ReadonlyArray<number> = thresholds;
    disconnect: () => void = disconnect;
    observe: (target: Element) => void = observe;
    takeRecords: () => IntersectionObserverEntry[] = takeRecords;
    unobserve: (target: Element) => void = unobserve;
  }

  Object.defineProperty(window, "IntersectionObserver", {
    writable: true,
    configurable: true,
    value: MockIntersectionObserver,
  });

  Object.defineProperty(global, "IntersectionObserver", {
    writable: true,
    configurable: true,
    value: MockIntersectionObserver,
  });
}

export { mockIntersectionObserver };
