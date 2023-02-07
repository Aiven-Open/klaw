import { createBrowserRouter, RouteObject } from "react-router-dom";
import NotFound from "src/app/pages/not-found";
import Topics from "src/app/pages/topics";
import AclRequest from "src/app/pages/topics/acl-request";
import ApprovalsPage from "src/app/pages/approvals";
import TopicApprovalsPage from "src/app/pages/approvals/topics";
import AclApprovalsPage from "src/app/pages/approvals/acls";
import SchemaApprovalsPage from "src/app/pages/approvals/schemas";
import ConnectorApprovalsPage from "src/app/pages/approvals/connectors";
import { getRouterBasename } from "src/config";
import RequestTopic from "src/app/pages/topics/request";
import SchemaRequest from "src/app/pages/topics/schema-request";
import {
  APPROVALS_TAB_ID_INTO_PATH,
  ApprovalsTabEnum,
  Routes,
} from "src/app/router_utils";

const routes: Array<RouteObject> = [
  // Login is currently the responsibility of the
  // Angular Klaw app
  // {
  //   path: "/login",
  // },
  {
    path: Routes.TOPICS,
    element: <Topics />,
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
    path: "*",
    element: <NotFound />,
  },
];

const router = createBrowserRouter(routes, {
  basename: getRouterBasename(),
});

export default router;
