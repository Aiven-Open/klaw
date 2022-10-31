import { createBrowserRouter, Navigate, RouteObject } from "react-router-dom";
import HomePage from "src/app/pages";
import Hello from "src/app/pages/Hello";

const routes: Array<RouteObject> = [
  {
    path: "/",
    element: <HomePage />,
  },
  {
    path: "/hello",
    element: <Hello />,
  },
  {
    path: "*",
    element: <Navigate to="/" />,
  },
];

const router = createBrowserRouter(routes);

export default router;
