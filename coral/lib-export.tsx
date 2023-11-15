import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import { ReactNode } from "react";
// eslint-disable-next-line
import {
  BrowseTopics as BrowseTopicsKlaw,
  BrowseTopicsProps as BrowseTopicsPropsKlaw,
} from "./src/app/features/topics/browse/BrowseTopics";

const withWrapper = ({ element }: { element: ReactNode }) => {
  const queryClient = new QueryClient();

  const WrappedElement = () => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{element}</BrowserRouter>
    </QueryClientProvider>
  );
  return <WrappedElement />;
};

const BrowseTopics = (props: BrowseTopicsProps) =>
  withWrapper({ element: <BrowseTopicsKlaw {...props} /> });

type BrowseTopicsProps = BrowseTopicsPropsKlaw;

export type { BrowseTopicsProps };
export { BrowseTopics };
