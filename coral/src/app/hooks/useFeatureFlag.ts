import { useState, useEffect } from "react";

enum FeatureFlag {
  TOPIC_REQUEST = "FEATURE_FLAG_TOPIC_REQUEST",
  TOPIC_ACL_REQUEST = "FEATURE_FLAG_TOPIC_ACL_REQUEST",
  TOPIC_SCHEMA_REQUEST = "FEATURE_FLAG_TOPIC_SCHEMA_REQUEST",
}

const useFeatureFlag = (flagName: FeatureFlag) => {
  const [state, setState] = useState(false);
  useEffect(() => {
    setState(process.env[flagName] === "true");
  }, [process.env[flagName]]);
  return state;
};

export { FeatureFlag };
export default useFeatureFlag;
