import React from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import router from "src/app/router";
import "@aivenio/design-system/dist/styles.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import "/src/app/main.module.css";
import { isUnauthorizedError } from "src/services/api";

const DEV_MODE = import.meta.env.DEV;

const root = createRoot(document.getElementById("root") as HTMLElement);

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      useErrorBoundary: (error: unknown) => isUnauthorizedError(error),
      retry: (failureCount: number, error: unknown) => {
        if (isUnauthorizedError(error)) {
          return false;
        }
        return failureCount < 2;
      },
    },
  },
});

function prepare(): Promise<void | ServiceWorkerRegistration> {
  if (DEV_MODE) {
    return import("src/services/api-mocks/browser").then(({ worker }) => {
      if ("start" in worker) {
        window.msw = worker;
        return worker.start({ onUnhandledRequest: "bypass" });
      }
    });
  }
  return Promise.resolve();
}

prepare().then(() => {
  root.render(
    <React.StrictMode>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
        {DEV_MODE && <ReactQueryDevtools />}
      </QueryClientProvider>
    </React.StrictMode>
  );
});
