import { rest } from "msw";
import { getHTTPBaseAPIUrl } from "src/config";
import { MswInstance } from "src/services/api-mocks/types";
import { KlawApiResponse } from "types/utils";

function mockGetTeams({
  mswInstance,
  response,
}: {
  mswInstance: MswInstance;
  response: {
    status?: number;
    data: KlawApiResponse<"getAllTeamsSUOnly"> | { message: string };
  };
}) {
  const url = `${getHTTPBaseAPIUrl()}/getAllTeamsSUOnly`;
  mswInstance.use(
    rest.get(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

const mockedTeamResponse = ["TEST_TEAM_01", "TEST_TEAM_02", "TEST_TEAM_03"];
const mockedAllTeamsResponse = [
  {
    teamname: "TEST_TEAM_01",
    teamphone: "000",
    contactperson: "000",
    teamId: 1,
    tenantId: 1,
    showDeleteTeam: true,
    tenantName: "tenant",
    envList: ["ALL"],
  },
  {
    teamname: "TEST_TEAM_02",
    teamphone: "000",
    contactperson: "000",
    teamId: 2,
    tenantId: 1,
    showDeleteTeam: true,
    tenantName: "tenant",
    envList: ["ALL"],
  },
  {
    teamname: "TEST_TEAM_03",
    teamphone: "000",
    contactperson: "000",
    teamId: 3,
    tenantId: 1,
    showDeleteTeam: true,
    tenantName: "tenant",
    envList: ["ALL"],
  },
];
export { mockGetTeams, mockedTeamResponse, mockedAllTeamsResponse };
