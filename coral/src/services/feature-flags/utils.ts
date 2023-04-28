import { FeatureFlag } from "src/services/feature-flags/types";
import { getProcessEnv } from "src/config";

function isFeatureFlagActive(flag: FeatureFlag) {
  return getProcessEnv()[flag] === "true";
}

export { isFeatureFlagActive };
