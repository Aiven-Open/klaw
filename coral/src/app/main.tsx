import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import router from "src/app/router";
import "@aivenio/design-system/dist/styles.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";

const DEV_MODE = import.meta.env.DEV;

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);

const queryClient = new QueryClient();

if (DEV_MODE) {
  await import("src/services/http-client/mocks/browser")
    .then(({ worker }) => {
      worker
        .start({
          onUnhandledRequest: "bypass",
        })
        .then();
    })
    .then(() => {
      root.render(
        <React.StrictMode>
          <QueryClientProvider client={queryClient}>
            <RouterProvider router={router} />
            <ReactQueryDevtools />
          </QueryClientProvider>
        </React.StrictMode>
      );
    });
} else {
  root.render(
    <React.StrictMode>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>
    </React.StrictMode>
  );
}
