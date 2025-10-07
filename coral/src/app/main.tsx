import { Context as AquariumContext } from "@aivenio/aquarium";
import "@aivenio/aquarium/dist/styles.css";
import {
  QueryCache,
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import React from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import router from "src/app/router";
import { isUnauthorizedError } from "src/services/api";
import "src/app/accessibility.module.css";
import "src/app/main.module.css";
import { AuthenticationRequiredAlert } from "src/app/components/AuthenticationRequiredAlert";
import { AuthProvider } from "src/app/context-provider/AuthProvider";
import { BasePage } from "src/app/layout/page/BasePage";
// https://github.com/microsoft/monaco-editor/tree/main/samples/browser-esm-vite-react
import "src/services/configure-monaco-editor";
import { isDevMode } from "src/services/is-dev-mode";

const DEV_MODE = isDevMode();

const root = createRoot(document.getElementById("root") as HTMLElement);

const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error) => {
      const isUnauthorized = isUnauthorizedError(error);
      if (isUnauthorized) {
        window.location.assign("/login");
        root.render(
          <BasePage
            headerContent={<></>}
            content={<AuthenticationRequiredAlert />}
          />
        );
      }
    },
  }),
  defaultOptions: {
    queries: {
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

root.render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <AquariumContext>
          <RouterProvider
            future={{ v7_startTransition: true }}
            router={router}
          />
          {DEV_MODE && <ReactQueryDevtools />}
        </AquariumContext>
      </AuthProvider>
    </QueryClientProvider>
  </React.StrictMode>
);
