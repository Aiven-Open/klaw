import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import BrowseTopics from "src/app/features/topics/browse/BrowseTopics";
import { Environment, getEnvironments } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { getTeams } from "src/domain/team";
import { mockedAllTeamsResponse } from "src/domain/team/team-api.msw";
import { AllTeams } from "src/domain/team/team-types";
import { getTopics } from "src/domain/topic";
import {
  mockedResponseMultiplePageTransformed,
  mockedResponseTransformed,
} from "src/domain/topic/topic-api.msw";
import { TopicApiResponse } from "src/domain/topic/topic-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { tabNavigateTo } from "src/services/test-utils/tabbing";

jest.mock("src/domain/team/team-api.ts");
jest.mock("src/domain/topic/topic-api.ts");
jest.mock("src/domain/environment/environment-api.ts");

const mockGetTeams = getTeams as jest.MockedFunction<typeof getTeams>;
const mockGetTopics = getTopics as jest.MockedFunction<typeof getTopics>;
const mockGetEnvironments = getEnvironments as jest.MockedFunction<
  typeof getEnvironments
>;

const filterByEnvironmentLabel = "Filter by Environment";
// const filterByTeamLabel = "Filter by team";

// @TODO find better location / handling for mocks
// depends on how we proceed
const mockedGetTopicsResponseSinglePage: TopicApiResponse =
  mockedResponseTransformed;
const mockedGetTopicsResponseMultiplePages: TopicApiResponse =
  mockedResponseMultiplePageTransformed;
const mockGetEnvironmentResponse: Environment[] =
  transformEnvironmentApiResponse(mockedEnvironmentResponse);
const mockGetTeamsResponse: AllTeams = mockedAllTeamsResponse;

describe("BrowseTopics.tsx", () => {
  beforeAll(() => {
    mockIntersectionObserver();
  });

  describe("handles successful response with one page", () => {
    beforeAll(async () => {
      mockGetTeams.mockResolvedValue(mockGetTeamsResponse);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTopics.mockResolvedValue(mockedGetTopicsResponseSinglePage);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-team-loading")
      );
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders a select element to filter topics by Kafka Environment", async () => {
      const select = await screen.findByRole("combobox", {
        name: filterByEnvironmentLabel,
      });

      expect(select).toBeEnabled();
    });

    // it("renders a select element to filter topics by team", () => {
    //   const select = screen.getByRole("combobox", {
    //     name: filterByTeamLabel,
    //   });

    //   expect(select).toBeEnabled();
    // });

    it("renders the topic table with information about the pages", async () => {
      const table = screen.getByRole("table", {
        name: `Topics overview`,
      });

      expect(table).toBeVisible();
    });

    it("shows topic names row headers", () => {
      const table = screen.getByRole("table", {
        name: "Topics overview",
      });

      const rowHeader = within(table).getByRole("cell", {
        name: mockedResponseTransformed.entries[0].topicName,
      });
      expect(rowHeader).toBeVisible();
    });

    it("does not render the pagination", () => {
      const pagination = screen.queryByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).not.toBeInTheDocument();
    });
  });

  describe("handles successful response with 4 pages", () => {
    beforeAll(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockedGetTopicsResponseMultiplePages);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterAll(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("renders the pagination with information about the pages", () => {
      const table = screen.getByRole("navigation", {
        name: "Pagination navigation, you're on page 2 of 4",
      });

      expect(table).toBeVisible();
    });
  });

  describe("handles user stepping through pagination", () => {
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockedGetTopicsResponseMultiplePages);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows page 2 as currently active page and the total page number", () => {
      const pagination = screen.getByRole("navigation", {
        name: /Pagination/,
      });

      expect(pagination).toHaveAccessibleName(
        "Pagination navigation, you're on page 2 of 4"
      );
    });

    it("fetches new data when user clicks on next page", async () => {
      const pageTwoButton = screen.getByRole("button", {
        name: "Go to next page, page 3",
      });

      await userEvent.click(pageTwoButton);

      expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
        currentPage: 3,
        environment: "ALL",
        searchTerm: "",
        // @TODO fix payload when API accept teamID and not teamName
        teamName: "ALL",
      });
    });
  });

  describe("handles user filtering topics by Environment", () => {
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue(mockGetEnvironmentResponse);
      mockGetTopics.mockResolvedValue(mockedResponseTransformed);

      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("shows a select element for Environments with `ALL` preselected", async () => {
      const select = await screen.findByRole("combobox", {
        name: filterByEnvironmentLabel,
      });

      expect(select).toHaveValue("ALL");
      expect(select).toHaveDisplayValue("All Environments");
    });

    it("changes active selected option when user selects `DEV`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByEnvironmentLabel,
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });
      expect(select).toHaveValue("ALL");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue("1");
    });

    it("fetches new data when user selects `DEV`", async () => {
      const select = screen.getByRole("combobox", {
        name: filterByEnvironmentLabel,
      });
      const option = within(select).getByRole("option", {
        name: "DEV",
      });

      await userEvent.selectOptions(select, option);

      expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
        currentPage: 1,
        environment: "1",
        searchTerm: "",
        // @TODO fix payload when API accept teamID and not teamName
        teamName: "ALL",
      });
    });
  });

  // @TODO: wait until API accepts teamId instead of teamname as param
  // describe("handles user filtering topics by team", () => {
  //   beforeEach(async () => {
  //     mockGetTeams.mockResolvedValue(mockGetTeamsResponse);
  //     mockGetEnvironments.mockResolvedValue([]);
  //     mockGetTopics.mockResolvedValue(mockedResponseTransformed);

  //     customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
  //     await waitForElementToBeRemoved(screen.getByText("Loading..."));
  //   });

  //   afterEach(() => {
  //     jest.clearAllMocks();
  //     cleanup();
  //   });

  //   it("shows a select element for team with `All teams` preselected", () => {
  //     const select = screen.getByRole("combobox", {
  //       name: filterByTeamLabel,
  //     });

  //     expect(select).toHaveValue("f5ed03b4-c0da-4b18-a534-c7e9a13d1342");
  //   });

  //   it("changes active selected option when user selects `TEST_TEAM_02`", async () => {
  //     const select = screen.getByRole("combobox", {
  //       name: filterByTeamLabel,
  //     });

  //     const option = await screen.findByRole("option", {
  //       name: "TEST_TEAM_02",
  //     });

  //     expect(select).toHaveValue("f5ed03b4-c0da-4b18-a534-c7e9a13d1342");

  //     await userEvent.selectOptions(select, option);

  //     expect(select).toHaveValue("TEST_TEAM_02");
  //   });

  //   it("shows an information that the list is updated after user selected a team", async () => {
  //     const select = screen.getByRole("combobox", {
  //       name: filterByTeamLabel,
  //     });
  //     const option = within(select).getByRole("option", {
  //       name: "TEST_TEAM_02",
  //     });
  //     expect(select).toHaveValue("f5ed03b4-c0da-4b18-a534-c7e9a13d1342");

  //     // I'm not happy with that, but it' s the only way I found
  //     // to make sure there's a information about the list being
  //     // updated rendered for the user. awaiting for the userEvent
  //     // will end in a state where the new data has already
  //     // arrived and is rendered, to the updating info will be gone
  //     userEvent.selectOptions(select, option);

  //     const updatingList = await screen.findByText("Filtering list...");
  //     expect(updatingList).toBeVisible();
  //   });

  //   it("fetches new data when user selects `TEST_TEAM_02`", async () => {
  //     const select = screen.getByRole("combobox", {
  //       name: filterByTeamLabel,
  //     });
  //     const option = within(select).getByRole("option", {
  //       name: "TEST_TEAM_02",
  //     });

  //     await userEvent.selectOptions(select, option);

  //     expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
  //       currentPage: 1,
  //       environment: "ALL",
  //       searchTerm: undefined,
  //       teamName: "TEST_TEAM_02",
  //     });
  //   });
  // });

  describe("handles user searching by topic name with search input", () => {
    const testSearchInput = "Searched for topic";
    beforeEach(async () => {
      mockGetTeams.mockResolvedValue([]);
      mockGetEnvironments.mockResolvedValue([]);
      mockGetTopics.mockResolvedValue(mockedResponseTransformed);
      customRender(<BrowseTopics />, { memoryRouter: true, queryClient: true });
      await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("fetches new data when when user enters text in input", async () => {
      const input = screen.getByRole("search");
      expect(input).toHaveValue("");

      await userEvent.type(input, testSearchInput);

      expect(input).toHaveValue(testSearchInput);

      await waitFor(() =>
        expect(mockGetTopics).toHaveBeenNthCalledWith(2, {
          currentPage: 1,
          environment: "ALL",
          searchTerm: "Searched for topic",
          // @TODO fix payload when API accept teamID and not teamName
          teamName: "ALL",
        })
      );
    });

    it("can navigate to search input with keyboard", async () => {
      const input = screen.getByRole("search");

      expect(input).toHaveValue("");

      await tabNavigateTo({ targetElement: input });

      expect(input).toHaveFocus();
    });
  });
});
