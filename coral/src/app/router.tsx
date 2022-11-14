import { createBrowserRouter, Navigate, RouteObject } from "react-router-dom";
import HomePage from "src/app/pages";
import Hello from "src/app/pages/Hello";
import Login from "src/app/pages/Login";
import { getRouterBasename } from "src/config";

const routes: Array<RouteObject> = [
  {
    path: "/",
    element: <HomePage />,
  },
  {
    path: "/login",
    element: <Login />,
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

const router = createBrowserRouter(routes, {
  basename: getRouterBasename(),
});

export default router;
