import { Context as AquariumContext } from "@aivenio/aquarium";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { updateTeam, getTeamsOfUser } from "src/domain/team/team-api";
import { cleanup, screen, within } from "@testing-library/react";
import { SwitchTeamsDropdown } from "src/app/features/team-info/SwitchTeamsDropdown";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { KlawApiResponse } from "types/utils";
import userEvent from "@testing-library/user-event";
import { KlawApiError } from "src/services/api";

jest.mock("src/domain/team/team-api.ts");

const mockGetTeamsOfUser = getTeamsOfUser as jest.MockedFunction<
  typeof getTeamsOfUser
>;
const mockUpdateTeam = updateTeam as jest.MockedFunction<typeof updateTeam>;

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const testUsername = "testuser";
const testCurrentTeam = "awesome team";

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

describe("SwitchTeamsDropdown", () => {
  describe("shows information about the users current team if the teamlist has less than 2 entries", () => {
    beforeAll(() => {
      mockGetTeamsOfUser.mockResolvedValue([]);
      mockUpdateTeam.mockImplementation(jest.fn());

      customRender(
        <AquariumContext>
          <SwitchTeamsDropdown
            userName={testUsername}
            currentTeam={testCurrentTeam}
          />
        </AquariumContext>,
        {
          queryClient: true,
        }
      );
    });
    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows the current team as text", async () => {
      const loading = screen.getByTestId("teams-loading");

      await waitForElementToBeRemoved(loading);

      const team = screen.getByText(testCurrentTeam);
      const button = screen.queryByRole("button", { name: "Change your team" });

      expect(team).toBeVisible();
      expect(button).not.toBeInTheDocument();
    });
  });

  describe("shows a dropdown to switch teams if the teamlist has more than 1 entry", () => {
    beforeAll(async () => {
      mockGetTeamsOfUser.mockResolvedValue(testTeams);
      mockUpdateTeam.mockImplementation(jest.fn());

      customRender(
        <AquariumContext>
          <SwitchTeamsDropdown
            userName={testUsername}
            currentTeam={testCurrentTeam}
          />
        </AquariumContext>,
        {
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByTestId("teams-loading"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a button to change team", async () => {
      const button = screen.getByRole("button", { name: "Change your team" });

      expect(button).toBeVisible();
    });

    it("show the current team hidden for screenreader", () => {
      const button = screen.getByRole("button", { name: "Change your team" });
      const text = within(button).getByText(testCurrentTeam);

      expect(text).toBeVisible();
      expect(text).toHaveAttribute("aria-hidden", "true");
    });

    it("indicates to user with assistive technology that button opens a menu", () => {
      const button = screen.getByRole("button", { name: "Change your team" });

      expect(button).toHaveAttribute("aria-haspopup", "true");
    });

    it("indicates to user with assistive technology that this menu is currently closed", () => {
      const button = screen.getByRole("button", { name: "Change your team" });

      expect(button).toHaveAttribute("aria-expanded", "false");
    });
  });

  describe("enables user to switch team", () => {
    const mockUpdateTeamFn = jest.fn();
    beforeEach(async () => {
      mockGetTeamsOfUser.mockResolvedValue(testTeams);
      mockUpdateTeam.mockImplementation(mockUpdateTeamFn);

      customRender(
        <AquariumContext>
          <SwitchTeamsDropdown
            userName={testUsername}
            currentTeam={testCurrentTeam}
          />
        </AquariumContext>,
        {
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByTestId("teams-loading"));
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("indicates to user with assistive technology that this menu is open when users clicks", async () => {
      const button = screen.getByRole("button", { name: "Change your team" });

      expect(button).toHaveAttribute("aria-expanded", "false");

      await userEvent.click(button);

      expect(button).toHaveAttribute("aria-expanded", "true");
    });

    it("shows a menu when clicking the button", async () => {
      const button = screen.getByRole("button", { name: "Change your team" });

      await userEvent.click(button);

      const menu = screen.getByRole("menu", { name: "Change your team" });

      expect(menu).toBeVisible();
    });

    it("shows a list of teams as menu items", async () => {
      const button = screen.getByRole("button", { name: "Change your team" });
      await userEvent.click(button);
      const menu = screen.getByRole("menu", { name: "Change your team" });
      const menuItems = within(menu).getAllByRole("menuitem");

      expect(menuItems).toHaveLength(testTeams.length);
      expect(menuItems[0]).toHaveTextContent(testTeams[0].teamname);
      expect(menuItems[0]).toHaveTextContent(testTeams[0].teamname);
    });

    it("shows a dialog for the user to confirm they want to switch team", async () => {
      const button = screen.getByRole("button", { name: "Change your team" });
      await userEvent.click(button);
      const menu = screen.getByRole("menu", { name: "Change your team" });
      const newTeamItem = within(menu).getByRole("menuitem", {
        name: testTeams[1].teamname,
      });

      await userEvent.click(newTeamItem);

      const dialog = screen.getByRole("dialog");

      expect(dialog).toBeVisible();
      expect(dialog).toHaveTextContent(
        "You are updating the team you are signed in with"
      );
    });

    it('removes modal without changes when user clicks "cancel"', async () => {
      const button = screen.getByRole("button", { name: "Change your team" });
      await userEvent.click(button);
      const menu = screen.getByRole("menu", { name: "Change your team" });
      const newTeamItem = within(menu).getByRole("menuitem", {
        name: testTeams[1].teamname,
      });

      await userEvent.click(newTeamItem);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const cancel = within(dialog).getByRole("button", { name: "Cancel" });

      await userEvent.click(cancel);

      // the dialog is remove fast, so using waitForElementToBeRemoved
      // would be flaky
      const dialogAfter = screen.queryByRole("dialog");
      expect(dialogAfter).not.toBeInTheDocument();

      expect(mockUpdateTeamFn).not.toHaveBeenCalled();
    });

    it('changes teams when user clicks "Change team"', async () => {
      const button = screen.getByRole("button", { name: "Change your team" });
      await userEvent.click(button);
      const menu = screen.getByRole("menu", { name: "Change your team" });
      const newTeamItem = within(menu).getByRole("menuitem", {
        name: testTeams[1].teamname,
      });

      await userEvent.click(newTeamItem);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const confirm = within(dialog).getByRole("button", {
        name: "Change team",
      });

      await userEvent.click(confirm);

      expect(dialog).not.toBeVisible();
      expect(mockUpdateTeamFn).toHaveBeenCalled();
    });
  });

  describe("handles an error while switching teams", () => {
    const mockError: KlawApiError = {
      message: "Sorry this is an error",
      success: false,
    };

    const originalConsoleError = console.error;
    beforeEach(async () => {
      console.error = jest.fn();
      mockGetTeamsOfUser.mockResolvedValue(testTeams);
      mockUpdateTeam.mockRejectedValue(mockError);

      customRender(
        <AquariumContext>
          <SwitchTeamsDropdown
            userName={testUsername}
            currentTeam={testCurrentTeam}
          />
        </AquariumContext>,
        {
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByTestId("teams-loading"));
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a toast notification with an error when changing teams fails", async () => {
      const button = screen.getByRole("button", { name: "Change your team" });
      await userEvent.click(button);
      const menu = screen.getByRole("menu", { name: "Change your team" });
      const newTeamItem = within(menu).getByRole("menuitem", {
        name: testTeams[1].teamname,
      });

      await userEvent.click(newTeamItem);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const confirm = within(dialog).getByRole("button", {
        name: "Change team",
      });

      await userEvent.click(confirm);

      expect(dialog).not.toBeVisible();
      expect(mockUpdateTeam).toHaveBeenCalled();
      expect(mockedUseToast).toHaveBeenCalledWith(
        expect.objectContaining({
          message: `Error while switching teams. ${mockError.message}`,
        })
      );
      expect(console.error).toHaveBeenCalledWith(mockError);
    });
  });
});
