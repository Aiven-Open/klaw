import useFeatureFlag, { FeatureFlag } from "src/app/hooks/useFeatureFlag";
import { renderHook } from "@testing-library/react";

describe("useFeatureFlag", () => {
  const originalEnv = process.env;

  afterEach(() => {
    process.env = originalEnv;
  });

  describe("when flag is not defined in process Environment", () => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { FEATURE_FLAG_TOPIC_REQUEST, ...rest } = originalEnv;
    beforeEach(() => {
      process.env = rest;
    });
    it("returns false", async () => {
      const {
        result: { current: value },
      } = await renderHook(() => useFeatureFlag(FeatureFlag.TOPIC_REQUEST));
      expect(value).toBe(false);
    });
  });
  describe('when flag is defined in process Environment with value "false"', () => {
    beforeEach(() => {
      process.env = {
        ...originalEnv,
        FEATURE_FLAG_TOPIC_REQUEST: "false",
      };
    });
    it("returns false", async () => {
      const {
        result: { current: value },
      } = await renderHook(() => useFeatureFlag(FeatureFlag.TOPIC_REQUEST));
      expect(value).toBe(false);
    });
  });
  describe('when flag is defined in process Environment with value "true"', () => {
    beforeEach(() => {
      process.env = {
        ...originalEnv,
        FEATURE_FLAG_TOPIC_REQUEST: "true",
      };
    });
    it("returns true", async () => {
      const {
        result: { current: value },
      } = await renderHook(() => useFeatureFlag(FeatureFlag.TOPIC_REQUEST));
      expect(value).toBe(true);
    });
  });
});
