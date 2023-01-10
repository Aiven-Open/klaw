# Feature flags

New features are introduced to Coral in small increments. With small increments, it likely that a certain feature is not working 100% from the end user point of view. One approach to the problem would be to build features in a feature branch and merge the feature branch to main after in being complete. However, that approach will lead into the need of constant rebasing. Instead of using feature branches, we decided to implement feature flag functionality. Feature flags enable Coral developers to merge incomplete pieces of a feature features to main without breaking the user experience of a production user.

The feature flags are controller in `vite.config.ts` as build time environment variables. We can choose in which modes the flag should be toggled on. For example, when Coral is run within the Core (Springboot), we want to toggle the flag off. However, development we want to toggle the feature on.

```
FEATURE_FLAG_TOPIC_REQUEST: ["development", "remote-api"].includes(mode).toString()
```

Introducing a new feature flag is relatively simple. First you need to define the environment variable that represents a flag in `vite.config.ts`. Then, you need to add the environment variable to be part of `FeatureFlag` enum defined in [useFeatureFlag.ts](../src/app/hooks/useFeatureFlag.ts). After that, you can access the feature flag in 

```
// useFeatureFlag.ts
enum FeatureFlag {
  TOPIC_REQUEST = "FEATURE_FLAG_TOPIC_REQUEST",
  <new_flag> = "FEATURE_FLAG_<new_flag>
}
```

```
// Component.tsx
const topicRequestEnabled = useFeatureFlag(FeatureFlag.TOPIC_REQUEST);
```