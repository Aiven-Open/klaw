import { createBrowserRouter, Navigate, RouteObject } from "react-router-dom";
import HomePage from "src/app/pages";
import Topics from "src/app/pages/topics";
import { getRouterBasename } from "src/config";

const routes: Array<RouteObject> = [
  {
    path: "/",
    element: <HomePage />,
  },
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
    element: <Navigate to="/" />,
  },
];

const router = createBrowserRouter(routes, {
  basename: getRouterBasename(),
});

export default router;
