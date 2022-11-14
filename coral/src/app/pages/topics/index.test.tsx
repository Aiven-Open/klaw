import { screen } from "@testing-library/react/pure";
import Topics from "src/app/pages/topics";
import { renderWithQueryClient } from "src/services/test-utils";
import { server } from "src/services/api-mocks/server";
import {
  mockedResponseTransformed,
  mockTopicGetRequest,
} from "src/domain/topics/topics-api.msw";
import {
  cleanup,
  waitForElementToBeRemoved,
} from "@testing-library/react/pure";

// mocks out components from Layout
// that clutter screen output
// should be a helper when it's needed again
// removes the need to add a memory router in the render
jest.mock("src/app/layout/Header");
jest.mock("src/app/layout/SideNavigation");

// This mirrors the formatting formation used in `/domain`
// it's a temp implementation here and will be removed
// as soon as we have the final API schema

describe("Topics", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  describe("renders default view with data from API", () => {
    // can be beforeAll when auto clean up setting is up to date
    // `render` from `renderWithQueryClientAndMemoryRouter` does
    // not import from `/pure` so that setting can't be used here
    beforeEach(async () => {
      mockTopicGetRequest({ mswInstance: server });
      renderWithQueryClient(<Topics />);
      await waitForElementToBeRemoved(screen.getByText("Loading..."));
    });

    afterEach(() => {
      server.resetHandlers();
      cleanup();
    });

    it("shows a headline", async () => {
      const headline = screen.getByRole("heading", {
        name: "Browse all topics",
      });

      expect(headline).toBeVisible();
    });

    it("shows a list of topics", async () => {
      const list = screen.getByRole("list");

      expect(list).toBeVisible();
    });

    it("shows list items for each topic", () => {
      const listItem = screen.getAllByRole("listitem");

      expect(listItem).toHaveLength(mockedResponseTransformed.length);
    });
  });
});
