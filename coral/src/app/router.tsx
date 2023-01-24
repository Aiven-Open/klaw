import { createBrowserRouter, RouteObject } from "react-router-dom";
import NotFound from "src/app/pages/not-found";
import Topics from "src/app/pages/topics";
import AclRequest from "src/app/pages/topics/acl-request";
import { getRouterBasename } from "src/config";
import RequestTopic from "src/app/pages/topics/request";

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
    path: "*",
    element: <NotFound />,
  },
];

const router = createBrowserRouter(routes, {
  basename: getRouterBasename(),
});

export default router;
