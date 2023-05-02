import { useState, useEffect } from "react";
import { FeatureFlag } from "src/services/feature-flags/types";
import { isFeatureFlagActive } from "src/services/feature-flags/utils";
import { getProcessEnv } from "src/config";

const useFeatureFlag = (flagName: FeatureFlag) => {
  const [state, setState] = useState(false);
  useEffect(() => {
    setState(isFeatureFlagActive(flagName));
  }, [getProcessEnv()[flagName]]);
  return state;
};

export default useFeatureFlag;
