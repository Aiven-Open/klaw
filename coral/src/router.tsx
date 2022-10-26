import { createBrowserRouter, Navigate, RouteObject } from "react-router-dom";
import HomePage from "./pages/index";
import HelloPage from "./pages/hello";

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
