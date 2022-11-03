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

function prepare(): Promise<void | ServiceWorkerRegistration> {
  if (DEV_MODE) {
    return import("src/domain/api-mocks/browser").then(({ worker }) => {
      if ("start" in worker) {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        //@ts-ignore
        window.msw = worker;
        return worker.start();
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
