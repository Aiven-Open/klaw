import { Sources } from "KlawProvider";
import { createContext, useContext } from "react";

export const SourcesContext = createContext<Sources>({
  getTopics: () => new Promise(() => []),
});
// Cannot work in React v17 (console)
export const useSourcesContext = () => useContext(SourcesContext);
export const SourcesProvider = ({
  children,
  sources,
}: React.PropsWithChildren<{ sources: Sources }>) => {
  return (
    <SourcesContext.Provider value={sources}>
      {children}
    </SourcesContext.Provider>
  );
};
