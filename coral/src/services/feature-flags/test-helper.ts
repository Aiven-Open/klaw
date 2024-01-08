import { FeatureFlag } from "src/services/feature-flags/types";
import * as featureFlagsUtils from "src/services/feature-flags/utils";

/** Use setupFeatureFlagMock() must be used for every render scope
 *  where you want to have a certain FeatureFlag active.
 *  Important: You can only set one FeatureFlag active at a time@
 */
const setupFeatureFlagMock = (flag: FeatureFlag, value: boolean) => {
  const spy = jest.spyOn(featureFlagsUtils, "isFeatureFlagActive");
  spy.mockImplementation((flagName) => (flagName === flag ? value : false));
};

export { setupFeatureFlagMock };
