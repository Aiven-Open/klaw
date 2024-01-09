// This is needed for Design System chart components
function mockResizeObserver({
  disconnect = () => null,
  observe = () => null,
  unobserve = () => null,
} = {}): void {
  class MockResizeObserver implements ResizeObserver {
    disconnect: () => void = disconnect;
    observe: (target: Element) => void = observe;
    unobserve: (target: Element) => void = unobserve;
  }

  Object.defineProperty(window, "ResizeObserver", {
    writable: true,
    configurable: true,
    value: MockResizeObserver,
  });

  Object.defineProperty(global, "ResizeObserver", {
    writable: true,
    configurable: true,
    value: MockResizeObserver,
  });
}

export { mockResizeObserver };
