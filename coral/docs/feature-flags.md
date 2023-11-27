# Feature flags

New features are introduced to Coral in small increments. With small increments, it likely that a certain feature is not working 100% from the end user point of view. One approach to the problem would be to build features in a feature branch and merge the feature branch to main after in being complete. However, that approach will lead into the need of constant rebasing. Instead of using feature branches, we decided to implement feature flag functionality.

Feature flags enable Coral developers to merge incomplete pieces of a feature features to main without breaking the user experience of a production user.

The feature flags are controller in [`vite.config.ts`](../vite.config.ts) as build time environment variables. We can choose in which modes the flag should be toggled on. For example, when Coral is run within the Core (Springboot), we want to toggle the flag off. However, development we want to toggle the feature on.

```
FEATURE_FLAG_TOPIC_REQUEST: ["development", "remote-api"].includes(mode).toString()
```

## Add a feature flag

Introducing a new feature flag requires only a few steps.

### 1. Create an environment variable

Define an environment variable represents a flag in [vite.config.ts](../vite.config.ts). Prefix your name with `FEATURE_FLAG_`.

Example:

```
"process.env": {
  ROUTER_BASENAME: getRouterBasename(environment),
  API_BASE_URL: getApiBaseUrl(environment),
  FEATURE_FLAG_EXAMPLE_FLAG: ["development", "remote-api"]
    .includes(mode)
    .toString()
}
```

### 2. Add your flag to the `FeatureFlag` enum

Add the environment variable to be part of `FeatureFlag` enum defined in [coral/src/services/feature-flags/types.ts](../src/services/feature-flags/types.ts) like this:

```
// src/services/feature-flags/types.ts
enum FeatureFlag {
  FEATURE_FLAG_EXAMPLE_FLAG = "FEATURE_FLAG_EXAMPLE_FLAG",
}
```

### 3. Use the feature flag

#### Using the feature flag in router

If you're developing a feature that required adding a new route, you can use our helper.

1. Add your new route
2. Add your route in [router-utils.ts](../src/app/router.tsx)

```tsx
enum Routes {
  TOPICS = "/topics",
  // ...
  YOUR_NEW_ROUTE = "/your-new-route",
}
```

Then add your route and the page component (in this example `<YourPageElement />`) to [router.tsx](../src/app/router.tsx), using our helper:

```tsx
import { createRouteBehindFeatureFlag } from "src/services/feature-flags/route-utils";

const routes: Array<RouteObject> = [
  {
    path: Routes.TOPICS,
    element: <Topics />,
  },
  // ...
  createRouteBehindFeatureFlag({
    path: Routes.YOUR_NEW_ROUTE,
    element: <YourPageElement />,
    featureFlag: FeatureFlag.YOUR_NEW_ROUTE,
    // Routes.TOPICS is an example, you can use any
    // existing route that makes the most sense in
    // your case
    redirectRouteWithoutFeatureFlag: Routes.TOPICS,
  }),
  //...
];
```

#### Using the feature flag in components

You can access the feature flag in your components etc. like this.

```
// Component.tsx
const topicRequestEnabled = useFeatureFlag(FeatureFlag.TOPIC_REQUEST);
```

## Note about testing

If you want to test your new feature, you have to add the feature flag in the test environment. To avoid adding env variables in tests or in various code files, we provide the helper `isFeatureFlagActive`.

You can mock that helper in tests like this:

```tsx
import { isFeatureFlagActive } from "coral/src/services/feature-flags/utils";

const isFeatureFlagActiveMock = jest.fn();

jest.mock("src/services/feature-flags/utils", () => ({
  isFeatureFlagActive: () => isFeatureFlagActiveMock(),
}));

describe("your test description", () => {
  isFeatureFlagActiveMock.mockReturnValue(false);
  // ...
});
```
