import { Team } from "src/domain/team/team-types";

const getTeams = async (): Promise<Team[]> => {
  return fetch(`/getAllTeamsSUOnly`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`msw error: ${response.statusText}`);
      }
      const result = await response.json();
      return result;
    })
    .catch((error) => {
      throw new Error(error);
    });
};

export { getTeams };
