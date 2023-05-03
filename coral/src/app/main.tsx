import { Context as AquariumContext } from "@aivenio/aquarium";
import "@aivenio/aquarium/dist/styles.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import React from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import router from "src/app/router";
import { isUnauthorizedError } from "src/services/api";
import "/src/app/accessibility.module.css";
import "/src/app/main.module.css";
// https://github.com/microsoft/monaco-editor/tree/main/samples/browser-esm-vite-react
import "/src/services/configure-monaco-editor";
import { AuthProvider } from "src/app/context-provider/AuthProvider";
import AuthenticationRequiredBoundary from "src/app/components/AuthenticationRequiredBoundary";

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
      refetchOnWindowFocus: false,
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
        <AuthenticationRequiredBoundary>
          <AuthProvider>
            <AquariumContext>
              <RouterProvider router={router} />
              {DEV_MODE && <ReactQueryDevtools />}
            </AquariumContext>
          </AuthProvider>
        </AuthenticationRequiredBoundary>
      </QueryClientProvider>
    </React.StrictMode>
  );
});
