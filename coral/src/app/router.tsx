import { createBrowserRouter, RouteObject } from "react-router-dom";
import NotFound from "src/app/pages/not-found";
import Topics from "src/app/pages/topics";
import { getRouterBasename } from "src/config";

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
    path: "*",
    element: <NotFound />,
  },
];

const router = createBrowserRouter(routes, {
  basename: getRouterBasename(),
});

export default router;
