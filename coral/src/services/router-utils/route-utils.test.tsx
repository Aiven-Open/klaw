import { screen } from "@testing-library/react";
import { ReactElement } from "react-markdown/lib/react-markdown";
import { Navigate, RouteObject } from "react-router-dom";
import { Routes } from "src/services/router-utils/types";
import {
  createPrivateRoute,
  createRouteBehindFeatureFlag,
  filteredRoutesForSuperAdmin,
  SuperadminRouteMap,
} from "src/services/router-utils/route-utils";
import { FeatureFlag } from "src/services/feature-flags/types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const isFeatureFlagActiveMock = jest.fn();
const mockAuthUser = jest.fn();
const mockUseToast = jest.fn();

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockUseToast,
}));

jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUser,
}));
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUser(),
}));
jest.mock("src/services/feature-flags/utils", () => ({
  isFeatureFlagActive: () => isFeatureFlagActiveMock(),
}));

describe("route-utils", () => {
  afterEach(() => {
    jest.resetAllMocks();
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

      beforeAll(() => {
        isFeatureFlagActiveMock.mockReturnValue(false);
        routeObject = createRouteBehindFeatureFlag({
          element: TestElement,
          featureFlag: TEST_FEATURE_FLAG,
          path: route,
          redirectRouteWithoutFeatureFlag: routeToRedirectWithoutFeatureFlag,
        });
      });

      afterAll(() => {
        jest.restoreAllMocks();
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

      beforeAll(() => {
        isFeatureFlagActiveMock.mockReturnValue(true);
        routeObject = createRouteBehindFeatureFlag({
          element: TestElement,
          featureFlag: TEST_FEATURE_FLAG,
          path: route,
          redirectRouteWithoutFeatureFlag: routeToRedirectWithoutFeatureFlag,
        });
      });

      afterAll(() => {
        jest.restoreAllMocks();
      });

      it("sets the right path in the object", () => {
        expect(routeObject.path).toEqual(route);
      });

      it("sets the given element as element in the object", () => {
        expect(routeObject.element).toEqual(TestElement);
      });
    });
  });

  describe("createPrivateRoute", () => {
    const TestElement = <div data-testid="test-element">test element</div>;
    const privateRoute = "/private-route" as Routes;

    describe("returns a route object with redirection when permission is false", () => {
      let routeObject: RouteObject;

      beforeAll(() => {
        mockAuthUser.mockReturnValue({
          permissions: { approveDeclineTopics: false },
        });
        routeObject = createPrivateRoute({
          path: privateRoute,
          element: TestElement,
          permission: "approveDeclineTopics",
          redirectUnauthorized: Routes.DASHBOARD,
        });
      });

      afterAll(() => {
        jest.restoreAllMocks();
      });

      it("Does not render the element and redirects when permission is false", () => {
        customRender(routeObject.element as ReactElement, {
          queryClient: true,
          memoryRouter: true,
        });

        expect(screen.queryByTestId("test-element")).not.toBeInTheDocument();
        expect(mockUseToast).toHaveBeenCalledWith({
          message: `Not authorized`,
          position: "bottom-left",
          variant: "danger",
        });
      });
    });

    describe("returns a route object with no redirection when permission is true", () => {
      let routeObject: RouteObject;

      beforeAll(() => {
        mockAuthUser.mockReturnValue({
          permissions: { approveDeclineTopics: true },
        });
        routeObject = createPrivateRoute({
          path: privateRoute,
          element: TestElement,
          permission: "approveDeclineTopics",
          redirectUnauthorized: Routes.DASHBOARD,
        });
      });

      afterAll(() => {
        jest.restoreAllMocks();
      });

      it("Renders the element when permission is true", () => {
        customRender(routeObject.element as ReactElement, {
          queryClient: true,
          memoryRouter: true,
        });

        expect(screen.queryByTestId("test-element")).toBeInTheDocument();
      });
    });
  });

  describe("filteredRoutesForSuperAdmin", () => {
    const testRoutes: Array<RouteObject> = [
      {
        path: "/",
        element: <div></div>,
        children: [
          {
            path: Routes.TOPICS,
            element: <div data-test-id={"topics"}></div>,
          },
          {
            path: Routes.TOPIC_OVERVIEW,
            element: <div data-test-id={"overview"}></div>,
          },
          {
            path: Routes.CONNECTOR_OVERVIEW,
            element: <div data-test-id={"connector-overview"}></div>,
          },
          {
            path: Routes.CONNECTORS,
            element: <div data-test-id={"connectors"}></div>,
            children: [
              {
                path: "/one",
                element: <div data-test-id={"child-one"}></div>,
              },
              {
                path: "/two",
                element: <div data-test-id={"child-two"}></div>,
              },
            ],
          },
        ],
      },
    ];

    const testAdminRoutes: SuperadminRouteMap = {
      [Routes.TOPIC_OVERVIEW]: {
        route: Routes.TOPIC_OVERVIEW,
        redirect: "/topicOverview",
      },
      [Routes.CONNECTOR_OVERVIEW]: {
        route: Routes.CONNECTORS,
        redirect: "",
        showNotFound: true,
      },
      [Routes.CONNECTORS]: {
        route: Routes.CONNECTORS,
        redirect: "/connectorOverview",
        removeChildren: true,
      },
    };

    describe("returns a updated route object based on a given map for superadmin routes", () => {
      const filteredRoutes = filteredRoutesForSuperAdmin(
        testRoutes,
        testAdminRoutes
      );

      it(`does not remove ${Routes.TOPICS} as it's not part of the map`, () => {
        const childOnRoutes = testRoutes[0].children
          ? testRoutes[0].children[0]
          : {};

        const childOnSuperAdminRoutes = filteredRoutes[0].children
          ? filteredRoutes[0].children[0]
          : {};

        expect(childOnRoutes).toStrictEqual(childOnSuperAdminRoutes);
      });

      it(`renders a component that handles a redirect for ${Routes.TOPIC_OVERVIEW}`, () => {
        const childOnRoutes = testRoutes[0].children
          ? testRoutes[0].children[1]
          : {};

        const childOnSuperAdminRoutes = filteredRoutes[0].children
          ? filteredRoutes[0].children[1]
          : {};

        expect(childOnRoutes).not.toEqual(childOnSuperAdminRoutes);

        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        expect(childOnSuperAdminRoutes.element?.type.name).toEqual(
          "SuperadminRoute"
        );

        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        expect(childOnSuperAdminRoutes.element.props).toEqual({
          children: <div data-test-id="overview" />,
          redirectSuperAdmin: "/topicOverview",
          showNotFound: undefined,
          removeChildren: undefined,
        });
      });

      it(`renders a component shows a NotFoundElement ${Routes.CONNECTOR_OVERVIEW}`, () => {
        const childOnRoutes = testRoutes[0].children
          ? testRoutes[0].children[2]
          : {};

        const childOnSuperAdminRoutes = filteredRoutes[0].children
          ? filteredRoutes[0].children[2]
          : {};

        expect(childOnRoutes).not.toEqual(childOnSuperAdminRoutes);

        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        expect(childOnSuperAdminRoutes.element?.type.name).toEqual(
          "SuperadminRoute"
        );

        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        expect(childOnSuperAdminRoutes.element.props).toEqual({
          children: <div data-test-id="connector-overview" />,
          redirectSuperAdmin: "",
          showNotFound: true,
          removeChildren: undefined,
        });
      });

      it(`renders a component that handles a redirect for ${Routes.CONNECTORS} and removes its children`, () => {
        const childOnRoutes = testRoutes[0].children
          ? testRoutes[0].children[3]
          : {};

        const childOnSuperAdminRoutes = filteredRoutes[0].children
          ? filteredRoutes[0].children[3]
          : {};

        expect(childOnRoutes).not.toEqual(childOnSuperAdminRoutes);

        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        expect(childOnSuperAdminRoutes.element?.type.name).toEqual(
          "SuperadminRoute"
        );

        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        expect(childOnSuperAdminRoutes.element.props).toEqual({
          children: <div data-test-id="connectors" />,
          redirectSuperAdmin: "/connectorOverview",
          showNotFound: undefined,
          removeChildren: true,
        });
      });
    });
  });
});
