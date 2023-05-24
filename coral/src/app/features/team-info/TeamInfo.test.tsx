import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, screen } from "@testing-library/react";
import { TeamInfo } from "src/app/features/team-info/TeamInfo";
import { AuthUser } from "src/domain/auth-user";
import { getTeamsOfUser } from "src/domain/team";
import { KlawApiResponse } from "types/utils";

const authUser: AuthUser = {
  canSwitchTeams: "false",
  teamId: "2",
  teamname: "awesome-bunch-of-people",
  username: "i-am-test-user",
};

const mockAuthUser = jest.fn();
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUser(),
}));

jest.mock("src/domain/team/team-api.ts");
const mockGetTeamsOfUser = getTeamsOfUser as jest.MockedFunction<
  typeof getTeamsOfUser
>;

const testTeams: KlawApiResponse<"getSwitchTeams"> = [
  {
    teamname: "new team",
    teamphone: "string",
    contactperson: "string",
    teamId: 1234,
    tenantId: 1,
    showDeleteTeam: false,
    tenantName: "string",
  },
  {
    teamname: "other new team",
    teamphone: "string",
    contactperson: "string",
    teamId: 4678,
    tenantId: 1,
    showDeleteTeam: false,
    tenantName: "string",
  },
];

describe("TeamInfo", () => {
  describe("shows a info about user's team when they can't switch teams", () => {
    mockAuthUser.mockReturnValue(authUser);

    beforeAll(() => {
      mockGetTeamsOfUser.mockResolvedValue([]);
      customRender(<TeamInfo />, {
        queryClient: true,
      });
    });
    afterAll(cleanup);

    it("shows the the team headline", () => {
      const teamHeading = screen.getByText("Team");

      // screen.debug();
      // since one part is wrapped in a span, we can't get the
      // full text line with getByText
      expect(teamHeading.parentNode).toHaveTextContent("Your");
    });

    it("shows team of the user as text", async () => {
      const team = await screen.findByText(authUser.teamname);

      expect(team).toBeVisible();
    });

    it("does not render a menu to change teams", async () => {
      await screen.findByText(authUser.teamname);
      const menuButton = screen.queryByRole("button", {
        name: "Change your team",
      });

      expect(menuButton).not.toBeInTheDocument();
    });
  });

  describe("shows a info about user's team and a dropdown to switch team", () => {
    mockAuthUser.mockReturnValue({ ...authUser, canSwitchTeams: "true" });

    beforeAll(() => {
      mockGetTeamsOfUser.mockResolvedValue(testTeams);
      customRender(<TeamInfo />, {
        queryClient: true,
      });
    });
    afterAll(cleanup);

    it("shows team of the user as menu", async () => {
      const team = await screen.findByText(authUser.teamname);
      const menuButton = await screen.findByRole("button", {
        name: "Change your team",
      });

      expect(menuButton).toBeEnabled();
      expect(team).toHaveAttribute("aria-hidden", "true");
    });
  });
});
