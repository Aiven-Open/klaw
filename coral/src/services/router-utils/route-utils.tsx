import { useToast } from "@aivenio/aquarium";
import { useEffect } from "react";
import { Navigate, RouteObject, useParams } from "react-router-dom";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import {
  Routes,
  APPROVALS_TAB_ID_INTO_PATH,
  ApprovalsTabEnum,
  CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
  ConnectorOverviewTabEnum,
  ENVIRONMENT_TAB_ID_INTO_PATH,
  EnvironmentsTabEnum,
  REQUESTS_TAB_ID_INTO_PATH,
  RequestsTabEnum,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
} from "src/services/router-utils/types";
import { Permission } from "src/domain/auth-user/auth-user-types";
import { FeatureFlag } from "src/services/feature-flags/types";
import { isFeatureFlagActive } from "src/services/feature-flags/utils";
import NotFound from "src/app/pages/not-found";
import isString from "lodash/isString";

type FeatureFlagRouteArgs = {
  path: Routes;
  element: JSX.Element;
  featureFlag: FeatureFlag;
  redirectRouteWithoutFeatureFlag: Routes;
  children?: RouteObject[];
};

type PrivateRouteArgs = {
  path: Routes;
  element: JSX.Element;
  permission: Permission;
  redirectUnauthorized: Routes;
  children?: RouteObject[];
};

type SuperadminRoute = {
  path: Routes;
  element: JSX.Element;
  redirect: string;
  children?: RouteObject[];
};

function isEnvironmentsTabEnum(value: unknown): value is EnvironmentsTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      ENVIRONMENT_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}
function isTopicsOverviewTabEnum(
  value: unknown
): value is TopicOverviewTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}

function isConnectorsOverviewTabEnum(
  value: unknown
): value is ConnectorOverviewTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}

function isRequestsTabEnum(value: unknown): value is RequestsTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      REQUESTS_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}

function isApprovalsTabEnum(value: unknown): value is ApprovalsTabEnum {
  if (isString(value)) {
    return Object.prototype.hasOwnProperty.call(
      APPROVALS_TAB_ID_INTO_PATH,
      value
    );
  }
  return false;
}
function createRouteBehindFeatureFlag({
  path,
  element,
  featureFlag,
  redirectRouteWithoutFeatureFlag,
  children,
}: FeatureFlagRouteArgs) {
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

const PrivateRoute = ({
  children,
  permission,
  redirectUnauthorized,
}: {
  children: React.ReactNode;
  permission: Permission;
  redirectUnauthorized: Routes;
}) => {
  const { permissions } = useAuthContext();
  const toast = useToast();
  const isAuthorized = permissions[permission];

  useEffect(() => {
    if (!isAuthorized) {
      toast({
        message: `Not authorized`,
        position: "bottom-left",
        variant: "danger",
      });

      return;
    }
  }, []);

  return isAuthorized ? (
    children
  ) : (
    <Navigate to={redirectUnauthorized} replace />
  );
};

function createPrivateRoute({
  path,
  element,
  permission,
  redirectUnauthorized,
  children,
}: PrivateRouteArgs) {
  return {
    path: path,
    element: (
      <PrivateRoute
        permission={permission}
        redirectUnauthorized={redirectUnauthorized}
      >
        {element}
      </PrivateRoute>
    ),
    ...(children && { children }),
  };
}

const SuperadminRoute = ({
  children,
  redirect,
  showNotFound,
  removeChildren,
}: {
  children: React.ReactNode;
  redirect: string;
  showNotFound?: boolean;
  removeChildren?: boolean;
}) => {
  const { userrole } = useAuthContext();
  const routeParams = useParams();

  const redirectStaticPart = redirect.split(":")[0];
  const param = redirect.split(":")[1];
  const redirectParam = routeParams[param] ? routeParams[param] : "";
  const redirectPath = `${redirectStaticPart}${redirectParam}`;

  const isSuperadmin = userrole !== "SUPERADMIN";

  console.log("userrole", userrole);
  useEffect(() => {
    if (isSuperadmin && !showNotFound) {
      window.location.replace(`${window.location.origin}${redirectPath}`);
    }
  });

  if (!isSuperadmin) {
    return children;
  }

  if (isSuperadmin && showNotFound) {
    console.log("here");
    return <NotFound />;
  }

  if (isSuperadmin && removeChildren) {
    return [];
  }

  return null;
};

type SuperadminRouteMap = {
  [Route in Routes]?: {
    route: Routes;
    redirect: string;
    showNotFound?: boolean;
    removeChildren?: boolean;
  };
};
function filteredRoutesForSuperAdmin(
  routes: Array<RouteObject>,
  redirectMap: SuperadminRouteMap
): Array<RouteObject> {
  return routes.map((route) => {
    const routePath = route.path as Routes;
    const redirect = redirectMap[routePath]?.redirect;

    if (route.children) {
      return {
        ...route,
        children: filteredRoutesForSuperAdmin(route.children, redirectMap),
        element:
          redirect !== undefined ? (
            <SuperadminRoute
              redirect={redirect}
              showNotFound={redirectMap[routePath]?.showNotFound}
              removeChildren={redirectMap[routePath]?.removeChildren}
            >
              {route.element}
            </SuperadminRoute>
          ) : (
            route.element
          ),
      };
    } else {
      if (routePath !== undefined && redirect !== undefined) {
        return {
          ...route,
          element: (
            <SuperadminRoute
              redirect={redirect}
              showNotFound={redirectMap[routePath]?.showNotFound}
            >
              {route.element}
            </SuperadminRoute>
          ),
        };
      } else {
        return route;
      }
    }
  });
}

export {
  createPrivateRoute,
  createRouteBehindFeatureFlag,
  filteredRoutesForSuperAdmin,
  isEnvironmentsTabEnum,
  isTopicsOverviewTabEnum,
  isConnectorsOverviewTabEnum,
  isRequestsTabEnum,
  isApprovalsTabEnum,
};
export type { SuperadminRouteMap };
