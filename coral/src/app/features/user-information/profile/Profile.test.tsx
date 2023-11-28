import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import { Profile } from "src/app/features/user-information/profile/Profile";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { getUser, User } from "src/domain/user";
import { KlawApiError } from "src/services/api";
import { userEvent } from "@testing-library/user-event";

const mockGetUser = getUser as jest.MockedFunction<typeof getUser>;
jest.mock("src/domain/user/user-api.ts");

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const testUser: User = {
  username: "c-pike",
  fullname: "Chris Pike",
  mailid: "cpike@ufp.org",
  role: "USER",
  switchTeams: false,
  team: "Enterprise",
  teamId: 2,
  tenantId: 0,
};

describe("Profile.tsx", () => {
  const user = userEvent.setup();

  describe("shows information that profile is loading", () => {
    beforeAll(() => {
      mockGetUser.mockResolvedValue(testUser);
      customRender(<Profile />, { queryClient: true });
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a visual hidden text", () => {
      const info = screen.getByText("User profile loading.");

      expect(info).toBeVisible();
      expect(info).toHaveClass("visually-hidden");
    });

    it("does not show a form while loading", () => {
      const form = screen.queryByRole("form");

      expect(form).not.toBeInTheDocument();
    });
  });

  describe("shows error message when loading profile fails", () => {
    const testError: KlawApiError = {
      message: "Oh no 😭",
      success: false,
    };

    const originalConsoleError = console.error;

    beforeAll(async () => {
      console.error = jest.fn();
      mockGetUser.mockRejectedValue(testError);
      customRender(<Profile />, { queryClient: true });

      await waitForElementToBeRemoved(
        screen.getByText("User profile loading.")
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
      console.error = originalConsoleError;
    });

    it("shows an error message", () => {
      const error = screen.getByRole("alert");
      expect(error).toBeVisible();

      expect(console.error).toHaveBeenCalledWith(testError);
    });

    it("does not show when there is an error", () => {
      const form = screen.queryByRole("form");

      expect(form).not.toBeInTheDocument();
    });
  });

  describe("shows all necessary elements in a form with all required user data", () => {
    beforeAll(async () => {
      mockGetUser.mockResolvedValue(testUser);
      customRender(<Profile />, { queryClient: true });

      await waitForElementToBeRemoved(
        screen.getByText("User profile loading.")
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a form", () => {
      const form = screen.getByRole("form", { name: "Update profile" });

      expect(form).toBeVisible();
    });

    it("shows a readonly textinput for username", () => {
      const userName = screen.getByRole("textbox", {
        name: "User name (read-only)",
      });

      expect(userName).toBeVisible();
      expect(userName).toHaveAttribute("readonly");
      expect(userName).toHaveValue(testUser.username);
    });

    it("shows an editable textinput with description for full name", () => {
      const fullName = screen.getByRole("textbox", { name: /Full name/ });

      expect(fullName).toBeEnabled();
      expect(fullName).toHaveAccessibleName(
        "Full name Can include uppercase and lowercase letters, accented characters (including umlauts), apostrophes, and spaces. It has to be at least 4 characters."
      );
      expect(fullName).toHaveValue(testUser.fullname);
    });

    it("shows an editable textinput for email address", () => {
      const emailAddress = screen.getByRole("textbox", {
        name: "Email address",
      });

      expect(emailAddress).toBeEnabled();
      expect(emailAddress).toHaveValue(testUser.mailid);
    });

    it("shows a readonly textinput for team", () => {
      const currentTeam = screen.getByRole("textbox", {
        name: "Team (read-only)",
      });

      expect(currentTeam).toBeVisible();
      expect(currentTeam).toHaveValue(testUser.team);
    });

    it("shows a readonly textinput for role", () => {
      const role = screen.getByRole("textbox", {
        name: "Role (read-only)",
      });

      expect(role).toBeVisible();
      expect(role).toHaveValue(testUser.role);
    });

    it("shows a button to change profile", () => {
      const button = screen.getByRole("button", { name: "Update profile" });

      expect(button).toBeEnabled();
    });

    it("does not show information that user can switch team if they can not", () => {
      const switchTeam = screen.queryByText(
        "User can switch teams (read-only)"
      );

      expect(switchTeam).not.toBeInTheDocument();
    });

    it("does not show a team list if user is not member of multiple teams", () => {
      const teams = screen.queryByText("Member of team (read-only)");

      expect(teams).not.toBeInTheDocument();
    });
  });

  describe("shows optional elements in a form when user can switch teams", () => {
    beforeAll(async () => {
      mockGetUser.mockResolvedValue({
        ...testUser,
        switchTeams: true,
        switchAllowedTeamNames: ["Discovery", "Enterprise"],
      });
      customRender(<Profile />, { queryClient: true });

      await waitForElementToBeRemoved(
        screen.getByText("User profile loading.")
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a disabled checkbox with information that user can switch teams", () => {
      const switchTeam = screen.getByRole("checkbox", {
        name: "User can switch teams (read-only)",
      });

      expect(switchTeam).toBeDisabled();
      expect(switchTeam).toBeChecked();
    });

    it("shows a list with teams the user is member of", () => {
      const teams = screen.getByRole("list", {
        name: "Member of team (read-only)",
      });

      expect(teams).toBeVisible();
    });

    it("shows all teams the user is member of", () => {
      const teams = screen.getByRole("list", {
        name: "Member of team (read-only)",
      });
      const allTeams = within(teams).getAllByRole("listitem");

      expect(allTeams).toHaveLength(2);
      expect(allTeams[0]).toHaveTextContent("Discovery");
      expect(allTeams[1]).toHaveTextContent("Enterprise");
    });
  });

  describe("shows notification when user changed nothing and submits form", () => {
    beforeEach(async () => {
      mockGetUser.mockResolvedValue({
        ...testUser,
        switchTeams: true,
        switchAllowedTeamNames: ["Discovery", "Enterprise"],
      });
      customRender(<Profile />, { queryClient: true });

      await waitForElementToBeRemoved(
        screen.getByText("User profile loading.")
      );
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows notification when user changes nothing", async () => {
      const button = screen.getByRole("button", { name: "Update profile" });

      await user.click(button);

      expect(mockedUseToast).toHaveBeenCalledWith(
        expect.objectContaining({
          message: "No changes were made to the topic.",
        })
      );
    });
  });
});
