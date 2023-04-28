import { isFeatureFlagActive } from "src/services/feature-flags/utils";
import { FeatureFlag } from "src/services/feature-flags/types";

// this avoids having to add a flag that is only used
// for testing purpose to our FeatureFlag enum
const TEST_FEATURE_FLAG = "TEST_FEATURE_FLAG" as unknown as FeatureFlag;

describe("utils", () => {
  const originalEnv = process.env;
  afterEach(() => {
    // This makes sure we're setting and resetting
    // the value explicitly and restore the original
    // process env after every test
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    process.env = originalEnv;
  });

  describe("isFeatureFlagActive", () => {
    it("returns true if the feature flag is set to 'true'", () => {
      process.env[TEST_FEATURE_FLAG] = "true";

      expect(isFeatureFlagActive(TEST_FEATURE_FLAG)).toBe(true);
    });

    it("returns false if the feature flag is set to 'false'", () => {
      process.env[TEST_FEATURE_FLAG] = "false";

      expect(isFeatureFlagActive(TEST_FEATURE_FLAG)).toBe(false);
    });

    it("returns false if the feature flag is not set at all", () => {
      process.env[TEST_FEATURE_FLAG] = undefined;

      expect(isFeatureFlagActive(TEST_FEATURE_FLAG)).toBe(false);
    });

    it("returns false if the feature flag is set to a value that is not 'true'", () => {
      process.env[TEST_FEATURE_FLAG] = "whynot";

      expect(isFeatureFlagActive(TEST_FEATURE_FLAG)).toBe(false);
    });
  });
});
