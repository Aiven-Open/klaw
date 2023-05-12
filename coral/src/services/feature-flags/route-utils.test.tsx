import { createRouteBehindFeatureFlag } from "src/services/feature-flags/route-utils";
import { FeatureFlag } from "src/services/feature-flags/types";
import { Routes } from "src/app/router_utils";
import { Navigate, RouteObject } from "react-router-dom";

const isFeatureFlagActiveMock = vi.fn();

vi.mock("src/services/feature-flags/utils", () => ({
  isFeatureFlagActive: () => isFeatureFlagActiveMock(),
}));

describe("route-utils", () => {
  afterEach(() => {
    vi.resetAllMocks();
  });

  describe("createRouteBehindFeatureFlag", () => {
    const TestElement = <div data-testid="test-element">test element</div>;

    // this avoids having to add a flag and routes that
    // are only used for testing purpose to our enums
    const route = "/with-feature-flag" as Routes;
    const routeToRedirectWithoutFeatureFlag = "/nope-sorry" as Routes;
    const TEST_FEATURE_FLAG = "TEST_FEATURE_FLAG" as unknown as FeatureFlag;

    describe("returns a route object with redirecting when feature flag is not active", () => {
      let routeObject: RouteObject;

      beforeEach(() => {
        isFeatureFlagActiveMock.mockReturnValue(false);
        routeObject = createRouteBehindFeatureFlag({
          element: TestElement,
          featureFlag: TEST_FEATURE_FLAG,
          path: route,
          redirectRouteWithoutFeatureFlag: routeToRedirectWithoutFeatureFlag,
        });
      });

      afterEach(() => {
        vi.restoreAllMocks();
      });

      it("sets the right path in the object", () => {
        expect(routeObject.path).toEqual(route);
      });

      it("sets the Navigate element from router to redirect user", () => {
        expect(routeObject.element).toEqual(
          <Navigate to={routeToRedirectWithoutFeatureFlag} />
        );
      });
    });

    describe("returns a route object with the feature flag protected component when feature flag is active", () => {
      let routeObject: RouteObject;

      beforeEach(() => {
        isFeatureFlagActiveMock.mockReturnValue(true);
        routeObject = createRouteBehindFeatureFlag({
          element: TestElement,
          featureFlag: TEST_FEATURE_FLAG,
          path: route,
          redirectRouteWithoutFeatureFlag: routeToRedirectWithoutFeatureFlag,
        });
      });

      afterEach(() => {
        vi.restoreAllMocks();
      });

      it("sets the right path in the object", () => {
        expect(routeObject.path).toEqual(route);
      });

      it("sets the given element as element in the object", () => {
        expect(routeObject.element).toEqual(TestElement);
      });
    });
  });
});
