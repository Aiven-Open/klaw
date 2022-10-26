import { createBrowserRouter, Navigate, RouteObject } from "react-router-dom";
import HomePage from "src/pages/index";
import HelloPage from "src/pages/hello";

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
