import { QueryClient } from "@tanstack/react-query";

function getQueryClientForTests() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        cacheTime: Infinity,
      },
    },
  });
}

export { getQueryClientForTests };
