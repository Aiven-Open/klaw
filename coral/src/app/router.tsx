import { createBrowserRouter, RouteObject } from "react-router-dom";
import NotFound from "src/app/pages/not-found";
import Topics from "src/app/pages/topics";
import AclRequest from "src/app/pages/topics/acl-request";
import { getRouterBasename } from "src/config";
import RequestTopic from "src/app/pages/topics/request";
import SchemaRequest from "src/app/pages/topics/schema-request";

const routes: Array<RouteObject> = [
  // Login is currently the responsibility of the
  // Angular Klaw app
  // {
  //   path: "/login",
  // },
  {
    path: "/topics",
    element: <Topics />,
  },
  {
    path: "/topics/request",
    element: <RequestTopic />,
  },
  {
    path: "/topic/:topicName/acl/request",
    element: <AclRequest />,
  },
  {
    path: "/topic/:topicName/request-schema",
    element: <SchemaRequest />,
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
