import { Navigate, RouteObject } from "react-router-dom";
import { isFeatureFlagActive } from "src/services/feature-flags/utils";
import { Routes } from "src/app/router_utils";
import { FeatureFlag } from "src/services/feature-flags/types";

type Args = {
  path: Routes;
  element: JSX.Element;
  featureFlag: FeatureFlag;
  redirectRouteWithoutFeatureFlag: Routes;
  children?: RouteObject[];
};

function createRouteBehindFeatureFlag({
  path,
  element,
  featureFlag,
  redirectRouteWithoutFeatureFlag,
  children,
}: Args): RouteObject {
  return {
    path: path,
    // if FEATURE_FLAG_<name> is not set to
    // true, we're not even rendering the page but
    // redirect user directly to /topics
    element: isFeatureFlagActive(featureFlag) ? (
      element
    ) : (
      <Navigate to={redirectRouteWithoutFeatureFlag} />
    ),
    ...(children && { children }),
  };
}

export { createRouteBehindFeatureFlag };
