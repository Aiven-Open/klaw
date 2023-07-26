import { buildUrl } from "src/services/url-coral-klaw-enabled";

const mockGetMetaEnv = jest.fn();
// { VITE_ROUTER_BASENAME: "/testname/" }

jest.mock("src/services/get-meta-env", () => ({
  getMetaEnv: () => mockGetMetaEnv(),
}));

describe("url-coral-klaw-enabled.ts", () => {
  describe('"buildUrl" creates a href string enabling our Angular/React switch', () => {
    const testHref = "/topics/test/me/please";

    const originalConsoleError = console.error;
    beforeEach(() => {
      console.error = jest.fn();
    });

    afterEach(() => {
      console.error = originalConsoleError;
      jest.resetAllMocks();
    });

    it("correctly transforms a given url if meta env is undefined and logs an error", () => {
      mockGetMetaEnv.mockReturnValue(undefined);

      const hrefString = buildUrl(testHref);

      expect(hrefString).toBe(testHref);
      expect(console.error).toHaveBeenCalledWith(
        "metaEnv should not be empty, please check vite meta data."
      );
    });

    it("correctly transforms a given url if VITE_ROUTER_BASENAME is undefined", () => {
      mockGetMetaEnv.mockReturnValue({ other: "data" });

      const hrefString = buildUrl(testHref);

      expect(hrefString).toBe(testHref);
      expect(console.error).not.toHaveBeenCalled();
    });

    it("correctly transforms a given url for VITE_ROUTER_BASENAME '/coral/", () => {
      mockGetMetaEnv.mockReturnValue({ VITE_ROUTER_BASENAME: "/coral/" });

      const hrefString = buildUrl(testHref);

      expect(hrefString).toBe(`/coral${testHref}`);
      expect(console.error).not.toHaveBeenCalled();
    });
  });
});
