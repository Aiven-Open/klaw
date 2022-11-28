import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { Environment, getEnvironments } from "src/domain/environment";

function useGetEnvironments(): UseQueryResult<Environment[]> {
  return useQuery<Environment[], Error>(
    ["topic-environments"],
    getEnvironments
  );
}

export { useGetEnvironments };
