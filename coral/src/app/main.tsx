import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import router from "src/app/router";
import "@aivenio/design-system/dist/styles.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";

// @TODO add vite env
const DEV_MODE = true;
const queryClient = new QueryClient();

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      {DEV_MODE && <RouterProvider router={router} />}
      <ReactQueryDevtools />
    </QueryClientProvider>
  </React.StrictMode>
);
