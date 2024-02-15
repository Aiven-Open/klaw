import { createBrowserRouter, RouteObject } from "react-router-dom";
import { ConnectorDocumentation } from "src/app/features/connectors/details/documentation/ConnectorDocumentation";
import { ConnectorHistory } from "src/app/features/connectors/details/history/ConnectorHistory";
import { ConnectorOverview } from "src/app/features/connectors/details/overview/ConnectorOverview";
import { ConnectorSettings } from "src/app/features/connectors/details/settings/ConnectorSettings";
import Layout from "src/app/layout/Layout";
import LayoutWithoutNav from "src/app/layout/LayoutWithoutNav";
import ActivityLogPage from "src/app/pages/activity-log";
import ApprovalsPage from "src/app/pages/approvals";
import AclApprovalsPage from "src/app/pages/approvals/acls";
import ConnectorApprovalsPage from "src/app/pages/approvals/connectors";
import SchemaApprovalsPage from "src/app/pages/approvals/schemas";
import TopicApprovalsPage from "src/app/pages/approvals/topics";
import { ClustersPage } from "src/app/pages/configuration/clusters";
import { AddClusterPage } from "src/app/pages/configuration/clusters/add";
import EnvironmentsPage from "src/app/pages/configuration/environments";
import KafkaEnvironmentsPage from "src/app/pages/configuration/environments/kafka";
import KafkaConnectEnvironmentsPage from "src/app/pages/configuration/environments/kafka-connect";
import SchemaRegistryEnvironmentsPage from "src/app/pages/configuration/environments/schema-registry";
import { TeamsPage } from "src/app/pages/configuration/teams";
import { UsersPage } from "src/app/pages/configuration/users";
import ConnectorsPage from "src/app/pages/connectors";
import { ConnectorDetailsPage } from "src/app/pages/connectors/details";
import ConnectorEditRequest from "src/app/pages/connectors/edit-request";
import { ConnectorPromotionRequestPage } from "src/app/pages/connectors/promotion-request";
import RequestConnector from "src/app/pages/connectors/request";
import DashboardPage from "src/app/pages/dashboard";
import NotFound from "src/app/pages/not-found";
import RequestsPage from "src/app/pages/requests";
import AclRequestsPage from "src/app/pages/requests/acls";
import ConnectorRequestsPage from "src/app/pages/requests/connectors";
import SchemaRequestsPage from "src/app/pages/requests/schemas";
import TopicRequestsPage from "src/app/pages/requests/topics";
import Topics from "src/app/pages/topics";
import AclRequest from "src/app/pages/topics/acl-request";
import { TopicDetailsPage } from "src/app/pages/topics/details";
import { TopicDocumentationPage } from "src/app/pages/topics/details/documentation";
import { TopicHistoryPage } from "src/app/pages/topics/details/history";
import { TopicMessagesPage } from "src/app/pages/topics/details/messages";
import { TopicOverviewPage } from "src/app/pages/topics/details/overview";
import { TopicDetailsSchemaPage } from "src/app/pages/topics/details/schema";
import { TopicSettingsPage } from "src/app/pages/topics/details/settings";
import { TopicSubscriptionsPage } from "src/app/pages/topics/details/subscriptions";
import TopicEditRequestPage from "src/app/pages/topics/edit-request";
import TopicPromotionRequestPage from "src/app/pages/topics/promotion-request";
import RequestTopic from "src/app/pages/topics/request";
import SchemaRequest from "src/app/pages/topics/schema-request";
import { ChangePassword } from "src/app/pages/user-information/change-password";
import { UserProfile } from "src/app/pages/user-information/profile";
import { TenantInfo } from "src/app/pages/user-information/tenant-info";
import { getRouterBasename } from "src/config";
import { FeatureFlag } from "src/services/feature-flags/types";
import {
  APPROVALS_TAB_ID_INTO_PATH,
  ApprovalsTabEnum,
  CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH,
  ConnectorOverviewTabEnum,
  ENVIRONMENT_TAB_ID_INTO_PATH,
  EnvironmentsTabEnum,
  REQUESTS_TAB_ID_INTO_PATH,
  RequestsTabEnum,
  Routes,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
} from "src/services/router-utils/types";
import {
  createPrivateRoute,
  createRouteBehindFeatureFlag,
  filteredRoutesForSuperAdmin,
  SuperadminRouteMap,
} from "src/services/router-utils/route-utils";
import { isFeatureFlagActive } from "src/services/feature-flags/utils";

const superAdminAccessCoralEnabled = isFeatureFlagActive(
  FeatureFlag.FEATURE_FLAG_SUPER_ADMIN_ACCESS_CORAL
);

const routes: Array<RouteObject> = [
  {
    path: "/",
    element: <Layout />,
    children: [
      {
        path: Routes.DASHBOARD,
        element: <DashboardPage />,
      },
      {
        path: Routes.TOPICS,
        element: <Topics />,
      },
      {
        path: Routes.TOPIC_OVERVIEW,
        element: <TopicDetailsPage />,
        children: [
          {
            path: TOPIC_OVERVIEW_TAB_ID_INTO_PATH[
              TopicOverviewTabEnum.OVERVIEW
            ],
            element: <TopicOverviewPage />,
            id: TopicOverviewTabEnum.OVERVIEW,
          },
          {
            path: TOPIC_OVERVIEW_TAB_ID_INTO_PATH[TopicOverviewTabEnum.ACLS],
            element: <TopicSubscriptionsPage />,
            id: TopicOverviewTabEnum.ACLS,
          },
          {
            path: TOPIC_OVERVIEW_TAB_ID_INTO_PATH[
              TopicOverviewTabEnum.MESSAGES
            ],
            element: <TopicMessagesPage />,
            id: TopicOverviewTabEnum.MESSAGES,
          },
          {
            path: TOPIC_OVERVIEW_TAB_ID_INTO_PATH[TopicOverviewTabEnum.SCHEMA],
            element: <TopicDetailsSchemaPage />,
            id: TopicOverviewTabEnum.SCHEMA,
          },
          {
            path: TOPIC_OVERVIEW_TAB_ID_INTO_PATH[
              TopicOverviewTabEnum.DOCUMENTATION
            ],
            element: <TopicDocumentationPage />,
            id: TopicOverviewTabEnum.DOCUMENTATION,
          },
          {
            path: TOPIC_OVERVIEW_TAB_ID_INTO_PATH[TopicOverviewTabEnum.HISTORY],
            element: <TopicHistoryPage />,
            id: TopicOverviewTabEnum.HISTORY,
          },
          {
            path: TOPIC_OVERVIEW_TAB_ID_INTO_PATH[
              TopicOverviewTabEnum.SETTINGS
            ],
            element: <TopicSettingsPage />,
            id: TopicOverviewTabEnum.SETTINGS,
          },
        ],
      },
      {
        path: Routes.CONNECTORS,
        element: <ConnectorsPage />,
      },
      {
        path: Routes.CONNECTOR_OVERVIEW,
        element: <ConnectorDetailsPage />,
        children: [
          {
            path: CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH[
              ConnectorOverviewTabEnum.OVERVIEW
            ],
            element: <ConnectorOverview />,
            id: ConnectorOverviewTabEnum.OVERVIEW,
          },
          {
            path: CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH[
              ConnectorOverviewTabEnum.DOCUMENTATION
            ],
            element: <ConnectorDocumentation />,
            id: ConnectorOverviewTabEnum.DOCUMENTATION,
          },
          {
            path: CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH[
              ConnectorOverviewTabEnum.HISTORY
            ],
            element: <ConnectorHistory />,
            id: ConnectorOverviewTabEnum.HISTORY,
          },
          {
            path: CONNECTOR_OVERVIEW_TAB_ID_INTO_PATH[
              ConnectorOverviewTabEnum.SETTINGS
            ],
            element: <ConnectorSettings />,
            id: ConnectorOverviewTabEnum.SETTINGS,
          },
        ],
      },
      {
        path: Routes.REQUESTS,
        element: <RequestsPage />,
        children: [
          {
            path: REQUESTS_TAB_ID_INTO_PATH[RequestsTabEnum.TOPICS],
            element: <TopicRequestsPage />,
            id: RequestsTabEnum.TOPICS,
          },
          {
            path: REQUESTS_TAB_ID_INTO_PATH[RequestsTabEnum.ACLS],
            element: <AclRequestsPage />,
            id: RequestsTabEnum.ACLS,
          },
          {
            path: REQUESTS_TAB_ID_INTO_PATH[RequestsTabEnum.SCHEMAS],
            element: <SchemaRequestsPage />,
            id: RequestsTabEnum.SCHEMAS,
          },
          {
            path: REQUESTS_TAB_ID_INTO_PATH[RequestsTabEnum.CONNECTORS],
            element: <ConnectorRequestsPage />,
            id: RequestsTabEnum.CONNECTORS,
          },
        ],
      },
      {
        path: Routes.APPROVALS,
        element: <ApprovalsPage />,
        children: [
          {
            path: APPROVALS_TAB_ID_INTO_PATH[ApprovalsTabEnum.TOPICS],
            element: <TopicApprovalsPage />,
            id: ApprovalsTabEnum.TOPICS,
          },
          {
            path: APPROVALS_TAB_ID_INTO_PATH[ApprovalsTabEnum.ACLS],
            element: <AclApprovalsPage />,
            id: ApprovalsTabEnum.ACLS,
          },
          {
            path: APPROVALS_TAB_ID_INTO_PATH[ApprovalsTabEnum.SCHEMAS],
            element: <SchemaApprovalsPage />,
            id: ApprovalsTabEnum.SCHEMAS,
          },
          {
            path: APPROVALS_TAB_ID_INTO_PATH[ApprovalsTabEnum.CONNECTORS],
            element: <ConnectorApprovalsPage />,
            id: ApprovalsTabEnum.CONNECTORS,
          },
        ],
      },
      {
        path: Routes.CONFIGURATION,
        children: [
          {
            path: Routes.ENVIRONMENTS,
            element: <EnvironmentsPage />,
            children: [
              {
                path: ENVIRONMENT_TAB_ID_INTO_PATH[EnvironmentsTabEnum.KAFKA],
                element: <KafkaEnvironmentsPage />,
                id: EnvironmentsTabEnum.KAFKA,
              },
              {
                path: ENVIRONMENT_TAB_ID_INTO_PATH[
                  EnvironmentsTabEnum.SCHEMA_REGISTRY
                ],
                element: <SchemaRegistryEnvironmentsPage />,
                id: EnvironmentsTabEnum.SCHEMA_REGISTRY,
              },
              {
                path: ENVIRONMENT_TAB_ID_INTO_PATH[
                  EnvironmentsTabEnum.KAFKA_CONNECT
                ],
                element: <KafkaConnectEnvironmentsPage />,
                id: EnvironmentsTabEnum.KAFKA_CONNECT,
              },
            ],
          },
          {
            path: Routes.TEAMS,
            element: <TeamsPage />,
          },
          {
            path: Routes.USERS,
            element: <UsersPage />,
          },
          {
            path: Routes.CLUSTERS,
            element: <ClustersPage />,
          },
        ],
      },
      {
        path: Routes.USER_INFORMATION,
        children: [
          {
            path: Routes.USER_PROFILE,
            element: <UserProfile />,
          },
          {
            path: Routes.USER_CHANGE_PASSWORD,
            element: <ChangePassword />,
          },
          {
            path: Routes.USER_TENANT_INFO,
            element: <TenantInfo />,
          },
        ],
      },
      {
        path: Routes.ACTIVITY_LOG,
        element: <ActivityLogPage />,
      },
    ],
  },
  {
    path: "/",
    element: <LayoutWithoutNav />,
    children: [
      {
        path: Routes.ACL_REQUEST,
        element: <AclRequest />,
      },
      {
        path: Routes.SCHEMA_REQUEST,
        element: <SchemaRequest />,
      },
      {
        path: Routes.CONNECTOR_REQUEST,
        element: <RequestConnector />,
      },
      {
        path: Routes.TOPIC_REQUEST,
        element: <RequestTopic />,
      },

      {
        path: Routes.TOPIC_ACL_REQUEST,
        element: <AclRequest />,
      },
      {
        path: Routes.TOPIC_SCHEMA_REQUEST,
        element: <SchemaRequest />,
      },
      {
        path: Routes.TOPIC_PROMOTION_REQUEST,
        element: <TopicPromotionRequestPage />,
      },
      {
        path: Routes.TOPIC_EDIT_REQUEST,
        element: <TopicEditRequestPage />,
      },
      {
        path: Routes.CONNECTOR_EDIT_REQUEST,
        element: <ConnectorEditRequest />,
      },
      {
        path: Routes.CONNECTOR_PROMOTION_REQUEST,
        element: <ConnectorPromotionRequestPage />,
      },
      createRouteBehindFeatureFlag({
        ...createPrivateRoute({
          path: Routes.ADD_CLUSTER,
          element: <AddClusterPage />,
          permission: "addDeleteEditClusters",
          redirectUnauthorized: Routes.CLUSTERS,
        }),
        featureFlag: FeatureFlag.FEATURE_FLAG_ADD_CLUSTER,
        redirectRouteWithoutFeatureFlag: Routes.CLUSTERS,
      }),
    ],
  },
  {
    path: "*",
    element: <Layout />,
    children: [{ path: "*", element: <NotFound /> }],
  },
];

/** `superadminRouteMap` is used as in-between-solution
 * to be able to filter "routes" and decide which ones
 * are accessible for role SUPERADMIN. It will be replaced
 * by a proper routing etc. based on fine-grained permissions.
 * The `redirect` placeholder (:placeHolder)
 * has to follow the same pattern as in our
 * `routes` list to be able to replace it correctly
 */
const superadminRouteMap: SuperadminRouteMap = {
  [Routes.TOPIC_OVERVIEW]: {
    route: Routes.TOPIC_OVERVIEW,
    redirect: "/topicOverview?topicname=:topicName",
  },
  [Routes.CONNECTOR_OVERVIEW]: {
    route: Routes.CONNECTOR_OVERVIEW,
    redirect: "/connectorOverview?connectorName=:connectorName",
  },
  [Routes.ENVIRONMENTS]: {
    route: Routes.ENVIRONMENTS,
    redirect: "/envs",
  },
  [Routes.TEAMS]: {
    route: Routes.TEAMS,
    redirect: "/teams",
  },
  [Routes.USERS]: {
    route: Routes.USERS,
    redirect: "/users",
  },
  [Routes.CLUSTERS]: {
    route: Routes.CLUSTERS,
    redirect: "/clusters",
  },

  // These are all routes that don't have an
  // equivalent in Klaw for SUPERADMIN
  // and are also unlikely for a superadmin
  // to click, so redirect goes to not found
  // could also be dashboard, but not found
  // gives additional information
  [Routes.CONNECTOR_REQUEST]: {
    route: Routes.CONNECTOR_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.CONNECTOR_EDIT_REQUEST]: {
    route: Routes.CONNECTOR_EDIT_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.CONNECTOR_PROMOTION_REQUEST]: {
    route: Routes.CONNECTOR_PROMOTION_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.TOPIC_REQUEST]: {
    route: Routes.TOPIC_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.ACL_REQUEST]: {
    route: Routes.ACL_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.TOPIC_ACL_REQUEST]: {
    route: Routes.TOPIC_ACL_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.TOPIC_SCHEMA_REQUEST]: {
    route: Routes.TOPIC_SCHEMA_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.TOPIC_PROMOTION_REQUEST]: {
    route: Routes.TOPIC_PROMOTION_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.TOPIC_EDIT_REQUEST]: {
    route: Routes.TOPIC_EDIT_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.SCHEMA_REQUEST]: {
    route: Routes.SCHEMA_REQUEST,
    redirect: "",
    showNotFound: true,
  },
  [Routes.REQUESTS]: {
    route: Routes.REQUESTS,
    redirect: "",
    showNotFound: true,
    removeChildren: true,
  },
  [Routes.APPROVALS]: {
    route: Routes.APPROVALS,
    redirect: "",
    showNotFound: true,
    removeChildren: true,
  },
};

// until we have permission in place like planned,
// we are filtering the `routes` object and handling
// routing based on role
const routeToUse = superAdminAccessCoralEnabled
  ? filteredRoutesForSuperAdmin(routes, superadminRouteMap)
  : routes;

const router = createBrowserRouter(routeToUse, {
  basename: getRouterBasename(),
});

export default router;
