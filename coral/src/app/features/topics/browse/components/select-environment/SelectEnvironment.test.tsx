import { cleanup, screen, waitFor } from "@testing-library/react";
import {
  ALL_ENVIRONMENTS_VALUE,
  Environment,
  mockGetEnvironments,
} from "src/domain/environment";
import SelectEnvironment from "src/app/features/topics/browse/components/select-environment/SelectEnvironment";
import { server } from "src/services/api-mocks/server";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { createEnvironment } from "src/domain/environment/environment-test-helper";

const filterLabel = "Filter by environment";
describe("SelectEnvironment.tsx", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  const mockedEnvironmentDev: Environment = createEnvironment({
    name: "DEV",
    id: "1",
  });
  const mockedEnvironmentTst: Environment = createEnvironment({
    name: "TST",
    id: "2",
  });

  describe("renders all necessary elements", () => {
    const mockedOnChange = jest.fn();

    beforeAll(async () => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      customRender(<SelectEnvironment onChange={mockedOnChange} />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );
    });

    afterAll(cleanup);

    it("shows a select element for Kafka environments", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for environments plus a option for `All environments`", () => {
      mockedEnvironmentResponse.forEach((environment) => {
        const option = screen.getByRole("option", {
          name: environment.name,
        });

        expect(option).toBeEnabled();
      });
      expect(screen.getAllByRole("option")).toHaveLength(
        mockedEnvironmentResponse.length + 1
      );
    });

    it("shows `All environments` as the active option one", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName("All environments");
    });

    it("updates the environment state for initial api call with value associated with `All Environments`", () => {
      expect(mockedOnChange).toHaveBeenCalledWith(ALL_ENVIRONMENTS_VALUE);
    });
  });

  describe("sets the active environment based on a query param", () => {
    const mockedOnChange = jest.fn();

    const mockedQueryParamDev = mockedEnvironmentDev.id;

    beforeEach(async () => {
      const routePath = `/?environment=${mockedQueryParamDev}`;

      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });

      customRender(<SelectEnvironment onChange={mockedOnChange} />, {
        memoryRouter: true,
        queryClient: true,
        customRoutePath: routePath,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it(`shows "${mockedEnvironmentDev.name}" as the active option one`, async () => {
      const option = await screen.findByRole("option", {
        name: mockedEnvironmentDev.name,
        selected: true,
      });
      expect(option).toBeVisible();
      expect(option).toHaveValue(mockedEnvironmentDev.id);
    });

    it("updates the environment state for api call", () => {
      expect(mockedOnChange).toHaveBeenCalledTimes(1);
    });
  });

  describe("handles user selecting a environment", () => {
    const mockedOnChange = jest.fn();

    beforeEach(async () => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      customRender(<SelectEnvironment onChange={mockedOnChange} />, {
        queryClient: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("updates state for api call when user selects a new environment", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = screen.getByRole("option", {
        name: mockedEnvironmentDev.name,
      });

      await userEvent.selectOptions(select, option);

      expect(mockedOnChange).toHaveBeenCalledWith("1");
    });

    it("sets the environment the user choose as active option", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });
      const option = screen.getByRole("option", {
        name: mockedEnvironmentDev.name,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(mockedEnvironmentDev.id);
    });
  });

  describe("updates the search param to preserve environment in url", () => {
    const mockedOnChange = jest.fn();

    beforeEach(async () => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      customRender(<SelectEnvironment onChange={mockedOnChange} />, {
        queryClient: true,
        browserRouter: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );
    });

    afterEach(() => {
      // resets url to get to clean state again
      window.history.pushState({}, "No page title", "/");
      jest.resetAllMocks();
      cleanup();
    });

    it("shows no search param by default", async () => {
      expect(window.location.search).toEqual("");
    });

    it(`sets "${mockedEnvironmentTst.name}" as search param when user selected it`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", {
        name: mockedEnvironmentTst.name,
      });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual(
          `?environment=${mockedEnvironmentTst.id}`
        );
      });
    });

    it("removes search param when user chooses All environment", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", { name: "All environments" });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual("");
      });
    });
  });
});
