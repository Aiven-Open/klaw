import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {BrowserRouter} from "react-router-dom";
import React, {ReactNode} from "react";
import {BrowseTopics as BrowseTopicsKlaw, BrowseTopicsProps as BrowseTopicsPropsKlaw} from "../../coral/src/app/features/topics/browse/BrowseTopics";

const withWrapper = ({element}: { element: ReactNode; }) => {
  const queryClient = new QueryClient()

  const WrappedElement = () => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        {element}
      </BrowserRouter>
      </QueryClientProvider>
  );
  return <WrappedElement />;
};

const BrowseTopics = (props: BrowseTopicsProps) => withWrapper({ element: <BrowseTopicsKlaw {...props} /> });

export { BrowseTopics }

interface BrowseTopicsProps extends BrowseTopicsPropsKlaw {}
export type { BrowseTopicsProps }