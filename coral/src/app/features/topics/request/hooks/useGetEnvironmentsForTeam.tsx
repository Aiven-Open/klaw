import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { Environment } from "src/domain/environment";
import { getEnvironmentsForTeam } from "src/domain/environment/environment-api";

function useGetEnvironmentsForTeam(): UseQueryResult<Environment[]> {
  return useQuery<Environment[], Error>(
    ["environments-for-team"],
    getEnvironmentsForTeam
  );
}

export { useGetEnvironmentsForTeam };
