import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { ReactNode } from "react";
// eslint-disable-next-line
import BrowseTopicsKlaw from "./src/app/features/topics/browse/BrowseTopics";
// eslint-disable-next-line
import { ApiProvider } from "./src/app/context-provider/ApiProvider";
// eslint-disable-next-line
import type { ApiConfig } from "./src/app/context-provider/ApiProvider";
// eslint-disable-next-line
import { TopicApiResponse } from "./src/domain/topic/topic-types";
// eslint-disable-next-line
import "./src/app/accessibility.module.css";
// eslint-disable-next-line
import BrowseConnectorsKlaw from "src/app/features/connectors/browse/BrowseConnectors";

type BrowseTopicsApiResponse = TopicApiResponse;

const withWrapper = ({
  element,
  currentLocation,
}: {
  element: ReactNode;
  currentLocation: string;
}) => {
  const queryClient = new QueryClient();

  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider
        router={createBrowserRouter([
          { path: currentLocation, element: element },
        ])}
      />
    </QueryClientProvider>
  );
};

const BrowseTopics = ({ currentLocation }: { currentLocation: string }) =>
  withWrapper({
    element: <BrowseTopicsKlaw />,
    currentLocation,
  });

const BrowseConnectors = ({ currentLocation }: { currentLocation: string }) =>
  withWrapper({
    element: <BrowseConnectorsKlaw />,
    currentLocation,
  });

BrowseConnectors;
export type { ApiConfig, BrowseTopicsApiResponse };
export { ApiProvider, BrowseTopics, BrowseConnectors };
