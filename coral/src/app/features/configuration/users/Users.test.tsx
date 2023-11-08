import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
  within,
} from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { KlawApiError } from "src/services/api";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { getUserList, User } from "src/domain/user";
import { Users } from "src/app/features/configuration/users/Users";
import { userEvent } from "@testing-library/user-event";

jest.mock("src/domain/user/user-api");

const mockGetUsers = getUserList as jest.MockedFunction<typeof getUserList>;

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

describe("Users.tsx", () => {
  describe("handles loading state", () => {
    beforeAll(() => {
      mockGetUsers.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [],
      });
      customRender(<Users />, { queryClient: true, memoryRouter: true });
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
      mockGetUsers.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: [],
      });
      customRender(<Users />, { queryClient: true, memoryRouter: true });
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

    const originalConsoleError = console.error;

    beforeAll(async () => {
      console.error = jest.fn();
      mockGetUsers.mockRejectedValue(testError);
      customRender(<Users />, { queryClient: true, memoryRouter: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
      console.error = originalConsoleError;
    });

    it("does not render the table", () => {
      const table = screen.queryByRole("table");
      expect(table).not.toBeInTheDocument();

      expect(console.error).toHaveBeenCalled();
    });

    it("shows an error alert", () => {
      const error = screen.getByRole("alert");

      expect(error).toBeVisible();
      expect(error).toHaveTextContent(testError.message);
      expect(console.error).toHaveBeenCalledWith(testError);
    });
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockIntersectionObserver();
      mockGetUsers.mockResolvedValue({
        currentPage: 1,
        totalPages: 1,
        entries: mockUsers,
      });
      customRender(<Users />, { queryClient: true, memoryRouter: true });
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
      mockGetUsers.mockResolvedValue({
        currentPage: 2,
        totalPages: 4,
        entries: mockUsers,
      });
      customRender(<Users />, { queryClient: true, memoryRouter: true });
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
      mockGetUsers.mockResolvedValue({
        currentPage: 3,
        totalPages: 5,
        entries: mockUsers,
      });
      customRender(<Users />, { queryClient: true, memoryRouter: true });
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

      await userEvent.click(pageTwoButton);

      expect(mockGetUsers).toHaveBeenNthCalledWith(2, {
        pageNo: "4",
      });
    });
  });
});
