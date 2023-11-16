import { QueryClient } from "@tanstack/query-core";
import { QueryClientProvider } from "@tanstack/react-query";
import React from "react";
import { SourcesProvider } from "sourcesContext";

export interface Sources {
  getTopics: <
    ReturnType extends Record<string, any>,
    ParamType extends Record<string, any>
  >(
    params: ParamType
  ) => Promise<{ topics: ReturnType[] }>;
}

interface KlawProviderProps {
  sources: Sources;
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount: number) => {
        return failureCount < 2;
      },
      refetchOnWindowFocus: false,
    },
  },
});

export const KlawProvider = ({
  children,
  sources,
}: React.PropsWithChildren<KlawProviderProps>) => {
  return (
    <QueryClientProvider client={queryClient}>
      <SourcesProvider sources={sources}>{children}</SourcesProvider>
    </QueryClientProvider>
  );
};