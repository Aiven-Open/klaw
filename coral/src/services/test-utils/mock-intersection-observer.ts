// This is needed for Design System <Table />
function mockIntersectionObserver(): void {
  Object.defineProperty(window, "IntersectionObserver", {
    writable: true,
    value: jest.fn().mockImplementation(() => ({
      observe: () => jest.fn(),
      disconnect: () => jest.fn(),
    })),
  });
}

export { mockIntersectionObserver };
