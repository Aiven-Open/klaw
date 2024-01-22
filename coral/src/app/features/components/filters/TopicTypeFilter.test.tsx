import { withFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { TopicTypeFilter } from "src/app/features/components/filters/TopicTypeFilter";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const WrapperFilter = withFiltersContext({ element: <TopicTypeFilter /> });

const filterLabel = "Filter by Topic type";
describe("TopicTypeFilter", () => {
  const user = userEvent.setup();

  describe("renders all necessary elements", () => {
    beforeAll(async () => {
      customRender(<WrapperFilter />, {
        memoryRouter: true,
      });
    });

    afterAll(cleanup);

    it("shows a select element for Topic type", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a option for ALL topic types", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = within(select).getByRole("option", {
        name: "All Topics",
      });
      expect(option).toBeEnabled();
    });

    it("renders a option for Consumer Topics", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = within(select).getByRole("option", {
        name: "Consumer",
      });
      expect(option).toBeEnabled();
    });

    it("renders a option for Producer Topics", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = within(select).getByRole("option", {
        name: "Producer",
      });
      expect(option).toBeEnabled();
    });

    it("renders the option for ALL topic types selected by default", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toHaveValue("ALL");
      expect(select).toHaveDisplayValue("All Topics");
    });
  });

  describe("sets the active Topic type based on a query param", () => {
    const producerTopic = "PRODUCER";
    beforeEach(async () => {
      const routePath = `/?topicType=${producerTopic}`;

      customRender(<WrapperFilter />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: routePath,
      });
    });

    afterEach(cleanup);

    it(`shows Producer as the selected topic type`, () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toHaveValue("PRODUCER");
      expect(select).toHaveDisplayValue("Producer");
    });
  });

  describe("handles user selecting a Topic type", () => {
    beforeEach(() => {
      customRender(<WrapperFilter />, {
        queryClient: true,
        browserRouter: true,
      });
    });

    afterEach(() => {
      // resets url state
      window.history.pushState({}, "No page title", "/");
      cleanup();
    });

    it("shows no search param by default", () => {
      expect(window.location.search).toEqual("");
    });

    it("sets the Topic type the user choose as selected option", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = screen.getByRole("option", {
        name: "Consumer",
      });

      await user.selectOptions(select, option);

      expect(select).toHaveValue("CONSUMER");
      expect(select).toHaveDisplayValue("Consumer");
    });

    it(`sets the correct topic type as search param when user selected it`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", {
        name: "Consumer",
      });

      await user.selectOptions(select, option);

      expect(select).toHaveValue("CONSUMER");

      await waitFor(() => {
        expect(window.location.search).toEqual(`?topicType=CONSUMER&page=1`);
      });
    });
  });
});
