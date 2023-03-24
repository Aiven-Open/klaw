import { cleanup, screen, waitFor } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { Environment, mockGetEnvironments } from "src/domain/environment";
import { mockedEnvironmentResponse } from "src/domain/environment/environment-api.msw";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { server } from "src/services/api-mocks/server";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const filterLabel = "Filter by Environment";
describe("EnvironmentFilter.tsx", () => {
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
    beforeAll(async () => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      customRender(<EnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
      );
    });

    afterAll(cleanup);

    it("shows a select element for Kafka Environments", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for environments plus a option for `All Environments`", () => {
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

    it("shows `All Environments` as the active option one", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName("All Environments");
    });
  });

  describe("sets the active environment based on a query param", () => {
    const mockedQueryParamDev = mockedEnvironmentDev.id;

    beforeEach(async () => {
      const routePath = `/?environment=${mockedQueryParamDev}`;

      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });

      customRender(<EnvironmentFilter />, {
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
  });

  describe("handles user selecting a environment", () => {
    beforeEach(async () => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      customRender(<EnvironmentFilter />, {
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
    beforeEach(async () => {
      mockGetEnvironments({
        mswInstance: server,
        response: { data: mockedEnvironmentResponse },
      });
      customRender(<EnvironmentFilter />, {
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

    it(`sets "${mockedEnvironmentTst.name}&page=1" as search param when user selected it`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", {
        name: mockedEnvironmentTst.name,
      });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual(
          `?environment=${mockedEnvironmentTst.id}&page=1`
        );
      });
    });

    it("removes environment search param when user chooses All environment", async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", { name: "All Environments" });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual("?page=1");
      });
    });
  });
});
