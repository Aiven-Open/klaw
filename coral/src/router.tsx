import { createBrowserRouter, Navigate, RouteObject } from "react-router-dom";
/* eslint-disable no-restricted-imports */
import HomePage from "src/pages";
import HelloPage from "src/pages/hello";
/* eslint-enable */

const routes: Array<RouteObject> = [
  {
    path: "/",
    element: <HomePage />,
  },
  {
    path: "/hello",
    element: <HelloPage />,
  },
  {
    path: "*",
    element: <Navigate to="/" />,
  },
];

const router = createBrowserRouter(routes);

export default router;
