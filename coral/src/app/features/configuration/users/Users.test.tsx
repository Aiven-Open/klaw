import {
  cleanup,
  screen,
  waitFor,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { KlawApiError } from "src/services/api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { getUserList, User } from "src/domain/user";
import { Users } from "src/app/features/configuration/users/Users";
import { userEvent } from "@testing-library/user-event";
import { getTeams, Team } from "src/domain/team";

jest.mock("src/domain/user/user-api");
jest.mock("src/domain/team/team-api.ts");

const mockGetUsers = getUserList as jest.MockedFunction<typeof getUserList>;
const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;

const userOne: User = {
  fullname: "Jean-Luc Picard",
  mailid: "jlpicard@ufp.org",
  role: "CAPTAIN",
  switchTeams: false,
  team: "Enterprise",
  teamId: 1,
  tenantId: 0,
  username: "js-picard",
};

const userTwo: User = {
  fullname: "Chris Pike",
  mailid: "cpike@ufp.org",
  role: "SUPERADMIN",
  switchAllowedTeamIds: [1, 2],
  switchAllowedTeamNames: ["Enterprise", "Discovery"],
  switchTeams: true,
  team: "Discovery",
  teamId: 2,
  tenantId: 0,
  username: "c-pike",
};

const mockUsers = [userOne, userTwo];

const teamOne: Team = {
  app: "",
  contactperson: "Picard",
  envList: [],
  serviceAccounts: {},
  showDeleteTeam: false,
  teamId: 1111,
  teammail: "",
  teamname: "NCC-1701-D",
  teamphone: "12345",
  tenantId: 0,
  tenantName: "UFP",
};
const teamTwo: Team = {
  contactperson: "Pike",
  showDeleteTeam: false,
  teamId: 2222,
  teamname: "NCC-1701",
  teamphone: "67890",
  tenantId: 0,
  tenantName: "default",
};

const mockedTeams = [teamOne, teamTwo];

describe("Users.tsx", () => {
  const user = userEvent.setup();

  describe("handles loading state", () => {
    beforeAll(() => {
      mockGetTeams.mockResolvedValue([]);
      mockGetUsers.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [],
      });
      customRender(<Users />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a loading information", () => {
      const loadingAnimation = screen.getByTestId("skeleton-table");
      expect(loadingAnimation).toBeVisible();
    });
  });

  describe("handles empty state", () => {
    beforeAll(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetUsers.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [],
      });
      customRender(<Users />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("does not render the table", () => {
      const table = screen.queryByRole("table");
      expect(table).not.toBeInTheDocument();
    });

    it("shows information about the empty state", () => {
      const heading = screen.getByRole("heading", { name: "No users" });
      expect(heading).toBeVisible();
    });
  });

  describe("handles error state", () => {
    const testError: KlawApiError = {
      message: "OH NO 😭",
      success: false,
    };

    beforeAll(async () => {
      jest.spyOn(console, "error").mockImplementationOnce((error) => error);
      mockGetTeams.mockResolvedValue([]);
      mockGetUsers.mockRejectedValue(testError);
      customRender(<Users />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
      expect(console.error).toHaveBeenCalledWith(testError);
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("does not render the table", () => {
      const table = screen.queryByRole("table");
      expect(table).not.toBeInTheDocument();
    });

    it("shows an error alert", () => {
      const error = screen.getByRole("alert");

      expect(error).toBeVisible();
      expect(error).toHaveTextContent(testError.message);
    });
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockIntersectionObserver();
      mockGetTeams.mockResolvedValue([]);
      mockGetUsers.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: mockUsers,
      });
      customRender(<Users />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a table with user overview", () => {
      const table = screen.getByRole("table", {
        name: "Users overview, page 1 of 1",
      });

      expect(table).toBeVisible();
    });

    it("does not render the pagination", () => {
      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).not.toBeInTheDocument();
    });

    it("shows select element for teams with All teams as default", () => {
      const select = screen.getByRole("combobox", { name: "Filter by team" });

      expect(select).toBeEnabled();
      expect(select).toHaveDisplayValue("All teams");
    });

    mockUsers.forEach((user) => {
      it(`renders the user name "${user.username}"`, () => {
        const table = screen.getByRole("table", {
          name: /Users overview/,
        });
        const cell = within(table).getByRole("cell", {
          name: user.username,
        });

        expect(cell).toBeVisible();
      });
    });
  });

  describe("handles successful response with 3 pages", () => {
    beforeAll(async () => {
      mockIntersectionObserver();
      mockGetTeams.mockResolvedValue([]);
      mockGetUsers.mockResolvedValue({
        currentPage: 2,
        totalPages: 4,
        entries: mockUsers,
      });
      customRender(<Users />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a table with user overview", () => {
      const table = screen.getByRole("table", {
        name: "Users overview, page 2 of 4",
      });

      expect(table).toBeVisible();
    });

    it("does the pagination with current and all pages", () => {
      const pagination = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 2 of 4",
      });

      expect(pagination).toBeVisible();
    });

    mockUsers.forEach((user) => {
      it(`renders the user name "${user.username}"`, () => {
        const table = screen.getByRole("table", {
          name: /Users overview/,
        });
        const cell = within(table).getByRole("cell", {
          name: user.username,
        });

        expect(cell).toBeVisible();
      });
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockIntersectionObserver();
      mockGetTeams.mockResolvedValue([]);
      mockGetUsers.mockResolvedValue({
        currentPage: 3,
        totalPages: 5,
        entries: mockUsers,
      });
      customRender(<Users />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 3 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 3 of 5"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 4",
      });

      await user.click(pageTwoButton);

      expect(mockGetUsers).toHaveBeenNthCalledWith(2, {
        pageNo: "4",
      });
    });
  });

  describe("enables user to filter by team", () => {
    beforeEach(async () => {
      mockIntersectionObserver();
      mockGetTeams.mockResolvedValue(mockedTeams);
      mockGetUsers.mockResolvedValue({
        currentPage: 3,
        totalPages: 5,
        entries: mockUsers,
      });
      customRender(<Users />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders a select with all teams fetched from the endpoint", () => {
      const select = screen.getByRole("combobox", {
        name: "Filter by team",
      });

      const options = within(select).getAllByRole("option");

      // one option for "All teams"
      expect(options).toHaveLength(mockedTeams.length + 1);
      expect(options[0]).toHaveValue("ALL");
      expect(options[1]).toHaveValue(mockedTeams[0].teamId.toString());
      expect(options[2]).toHaveValue(mockedTeams[1].teamId.toString());
    });

    it("fetches new data when user filters by a team", async () => {
      const teamToSelect = mockedTeams[0].teamname;

      const select = screen.getByRole("combobox", {
        name: "Filter by team",
      });

      const option = within(select).getByRole("option", { name: teamToSelect });

      await user.selectOptions(select, option);

      //first call on load
      expect(mockGetUsers).toHaveBeenNthCalledWith(2, {
        pageNo: "1",
        searchUserParam: undefined,
        teamId: mockedTeams[0].teamId,
      });
    });

    it("removes teamName as url param when user chooses All teams", async () => {
      const teamToSelect = mockedTeams[0].teamname;
      const select = screen.getByRole("combobox", {
        name: "Filter by team",
      });
      const optionOne = within(select).getByRole("option", {
        name: teamToSelect,
      });

      await user.selectOptions(select, optionOne);

      //first call on load
      expect(mockGetUsers).toHaveBeenNthCalledWith(2, {
        pageNo: "1",
        searchUserParam: undefined,
        teamId: mockedTeams[0].teamId,
      });

      const optionTwo = within(select).getByRole("option", {
        name: "All teams",
      });

      await user.selectOptions(select, optionTwo);

      //first call on load, second for the first filter
      expect(mockGetUsers).toHaveBeenNthCalledWith(3, {
        pageNo: "1",
      });
    });
  });

  describe("enables user to search for user name", () => {
    beforeEach(async () => {
      mockIntersectionObserver();
      mockGetTeams.mockResolvedValue(mockedTeams);
      mockGetUsers.mockResolvedValue({
        currentPage: 3,
        totalPages: 5,
        entries: mockUsers,
      });
      customRender(<Users />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders a search field for username", () => {
      const search = screen.getByRole("search", {
        name: "Search username",
      });

      expect(search).toBeEnabled();
      expect(search).toHaveAccessibleDescription(
        'Partial match for username Searching starts automatically with a little delay while typing. Press "Escape"' +
          " to delete all your input."
      );
    });

    it("fetches new data when user searches for username", async () => {
      const usernameSearch = "myname";

      const search = screen.getByRole("search", {
        name: "Search username",
      });

      await user.type(search, usernameSearch);

      await waitFor(() => {
        expect(mockGetUsers).toHaveBeenCalledWith({
          pageNo: "1",
          searchUserParam: usernameSearch,
        });
      });
    });

    it("removes username search as url param when user deletes input", async () => {
      const usernameSearch = "myname";

      const search = screen.getByRole("search", {
        name: "Search username",
      });

      await user.type(search, usernameSearch);

      await waitFor(() => {
        // first call is on load
        expect(mockGetUsers).toHaveBeenNthCalledWith(2, {
          pageNo: "1",
          searchUserParam: usernameSearch,
        });
      });

      await user.clear(search);

      await waitFor(() => {
        // first call is on load, second for search
        expect(mockGetUsers).toHaveBeenNthCalledWith(3, {
          pageNo: "1",
        });
      });
    });
  });
});
