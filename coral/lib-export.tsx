import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import { ReactNode } from "react";
// eslint-disable-next-line
import { BrowseTopics as BrowseTopicsKlaw } from "./src/app/features/topics/browse/BrowseTopics";
// eslint-disable-next-line
import { ApiProvider } from "./src/app/context-provider/ApiProvider";
// eslint-disable-next-line
import type { ApiConfig } from "./src/app/context-provider/ApiProvider";
// eslint-disable-next-line
import { TopicApiResponse } from "./src/domain/topic/topic-types";
// eslint-disable-next-line
import "./src/app/accessibility.module.css";

type BrowseTopicsApiResponse = TopicApiResponse;
const withWrapper = ({ element }: { element: ReactNode }) => {
  const queryClient = new QueryClient();

  const WrappedElement = () => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{element}</BrowserRouter>
    </QueryClientProvider>
  );
  return <WrappedElement />;
};

const BrowseTopics = () => withWrapper({ element: <BrowseTopicsKlaw /> });

export type { ApiConfig, BrowseTopicsApiResponse };
export { ApiProvider, BrowseTopics };
