import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { useEffect } from "react";
import {
  Environment,
  getEnvironments,
  mockGetEnvironments,
} from "src/domain/environment";

function useGetEnvs(): UseQueryResult<Environment[]> {
  // everything in useEffect is used to mock the api call
  // and can be removed once the real api is connected
  useEffect(() => {
    const browserEnvWorker = window.msw;

    if (browserEnvWorker) {
      mockGetEnvironments({ mswInstance: browserEnvWorker });
    }
  }, []);

  return useQuery<Environment[], Error>(["topic-envs"], () =>
    getEnvironments()
  );
}

export { useGetEnvs };
