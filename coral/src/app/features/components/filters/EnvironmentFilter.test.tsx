import { cleanup, screen, waitFor } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { userEvent } from "@testing-library/user-event";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { withFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import {
  getAllEnvironmentsForTopicAndAcl,
  getAllEnvironmentsForConnector,
} from "src/domain/environment";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { KlawApiError } from "src/services/api";
import { getAllEnvironments } from "src/domain/environment/environment-api";

jest.mock("src/domain/environment/environment-api.ts");

const mockGetEnvironments =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockGetSyncConnectorsEnvironments =
  getAllEnvironmentsForConnector as jest.MockedFunction<
    typeof getAllEnvironmentsForConnector
  >;

const mockGetAllEnvironments = getAllEnvironments as jest.MockedFunction<
  typeof getAllEnvironments
>;

const mockEnvironments = [
  createMockEnvironmentDTO({
    name: "DEV",
    id: "1",
  }),
  createMockEnvironmentDTO({
    name: "TST",
    id: "2",
  }),
];

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const filterLabel = "Filter by Environment";

const WrappedEnvironmentFilter = withFiltersContext({
  element: <EnvironmentFilter environmentsFor={"TOPIC_AND_ACL"} />,
});

describe("EnvironmentFilter.tsx", () => {
  describe("uses different endpoint to fetch environments dependent on props", () => {
    beforeEach(() => {
      mockGetEnvironments.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetAllEnvironments.mockResolvedValue([]);
    });
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("fetches environments for 'TOPIC_AND_ACL'", () => {
      const WrappedEnvironmentFilter = withFiltersContext({
        element: <EnvironmentFilter environmentsFor={"TOPIC_AND_ACL"} />,
      });
      customRender(<WrappedEnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });

      expect(mockGetEnvironments).toHaveBeenCalled();
      expect(mockGetSyncConnectorsEnvironments).not.toHaveBeenCalled();
    });

    it("fetches environments for 'SCHEMA'", () => {
      const WrappedEnvironmentFilter = withFiltersContext({
        element: <EnvironmentFilter environmentsFor={"SCHEMA"} />,
      });
      customRender(<WrappedEnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });

      expect(mockGetEnvironments).toHaveBeenCalled();
      expect(mockGetSyncConnectorsEnvironments).not.toHaveBeenCalled();
      expect(mockGetAllEnvironments).not.toHaveBeenCalled();
    });

    it("fetches environments for 'CONNECTOR'", () => {
      const WrappedEnvironmentFilter = withFiltersContext({
        element: <EnvironmentFilter environmentsFor={"CONNECTOR"} />,
      });
      customRender(<WrappedEnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });

      expect(mockGetSyncConnectorsEnvironments).toHaveBeenCalled();
      expect(mockGetEnvironments).not.toHaveBeenCalled();
      expect(mockGetAllEnvironments).not.toHaveBeenCalled();
    });

    it("fetches environments for 'ALL'", () => {
      const WrappedEnvironmentFilter = withFiltersContext({
        element: <EnvironmentFilter environmentsFor={"ALL"} />,
      });
      customRender(<WrappedEnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
      });

      expect(mockGetAllEnvironments).toHaveBeenCalled();
      expect(mockGetSyncConnectorsEnvironments).not.toHaveBeenCalled();
      expect(mockGetEnvironments).not.toHaveBeenCalled();
    });
  });

  describe("renders all necessary elements for 'TOPIC_AND_ACL' (same as 'CONNECTOR')", () => {
    beforeAll(async () => {
      mockGetEnvironments.mockResolvedValue(mockEnvironments);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetAllEnvironments.mockResolvedValue([]);

      customRender(<WrappedEnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("async-select-loading-environments")
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a select element for Kafka Environments", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for environments plus a option for `All Environments`", () => {
      mockEnvironments.forEach((environment) => {
        const option = screen.getByRole("option", {
          name: environment.name,
        });

        expect(option).toBeEnabled();
      });
      expect(screen.getAllByRole("option")).toHaveLength(
        mockEnvironments.length + 1
      );
    });

    it("shows `All Environments` as the active option one", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName("All Environments");
    });
  });

  describe("renders all necessary elements for 'SCHEMA'", () => {
    const associatedEnvDev = {
      id: "111",
      name: "DEV_SCH",
    };

    const associatedEnvTst = {
      id: "222",
      name: "TST_SCH",
    };

    const mockEnvironmentsSchema = [
      createMockEnvironmentDTO({
        name: "DEV",
        id: "1",
        associatedEnv: associatedEnvDev,
      }),
      createMockEnvironmentDTO({
        name: "TST",
        id: "2",
        associatedEnv: associatedEnvTst,
      }),

      createMockEnvironmentDTO({
        name: "DIV",
        id: "3",
      }),
    ];

    beforeAll(async () => {
      mockGetEnvironments.mockResolvedValue(mockEnvironmentsSchema);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetAllEnvironments.mockResolvedValue([]);

      const WrappedEnvironmentFilter = withFiltersContext({
        element: <EnvironmentFilter environmentsFor={"SCHEMA"} />,
      });

      customRender(<WrappedEnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("async-select-loading-environments")
      );
    });

    afterAll(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a select element for Kafka Environments", () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      expect(select).toBeEnabled();
    });

    it("renders a list of options for environments with associated envs plus a option for `All Environments`", () => {
      const filteredEnvs = mockEnvironmentsSchema.filter(
        (env) => env.associatedEnv
      );

      filteredEnvs.forEach((environment) => {
        const option = screen.getByRole("option", {
          // associated env is not undefined in this test,
          // no need for warning
          // eslint-disable-next-line @typescript-eslint/ban-ts-comment
          // @ts-ignore
          name: environment.associatedEnv.name,
        });

        expect(option).toBeEnabled();
      });

      expect(screen.getAllByRole("option")).toHaveLength(
        filteredEnvs.length + 1
      );
    });

    it("shows `All Environments` as the active option one", () => {
      const option = screen.getByRole("option", {
        selected: true,
      });
      expect(option).toHaveAccessibleName("All Environments");
    });

    it("shows the name of the associated env as option", () => {
      const option = screen.getByRole("option", {
        name: associatedEnvDev.name,
      });

      expect(option).toHaveAccessibleName(associatedEnvDev.name);
      expect(option).toHaveValue(associatedEnvDev.id);
    });
  });

  describe("sets the active environment based on a query param", () => {
    const mockedQueryParamDev = mockEnvironments[0].id;

    beforeEach(async () => {
      const routePath = `/?environment=${mockedQueryParamDev}`;

      mockGetEnvironments.mockResolvedValue(mockEnvironments);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetAllEnvironments.mockResolvedValue([]);

      customRender(<WrappedEnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
        customRoutePath: routePath,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("async-select-loading-environments")
      );
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it(`shows "${mockEnvironments[0].name}" as the active option one`, async () => {
      const option = await screen.findByRole("option", {
        name: mockEnvironments[0].name,
        selected: true,
      });
      expect(option).toBeVisible();
      expect(option).toHaveValue(mockEnvironments[0].id);
    });
  });

  describe("handles user selecting a environment", () => {
    beforeEach(async () => {
      mockGetEnvironments.mockResolvedValue(mockEnvironments);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetAllEnvironments.mockResolvedValue([]);

      customRender(<WrappedEnvironmentFilter />, {
        queryClient: true,
        aquariumContext: true,
        memoryRouter: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("async-select-loading-environments")
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
        name: mockEnvironments[1].name,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(mockEnvironments[1].id);
    });
  });

  describe("updates the search param to preserve environment in url", () => {
    beforeEach(async () => {
      mockGetEnvironments.mockResolvedValue(mockEnvironments);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
      mockGetAllEnvironments.mockResolvedValue([]);

      customRender(<WrappedEnvironmentFilter />, {
        queryClient: true,
        aquariumContext: true,
        browserRouter: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("async-select-loading-environments")
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

    it(`sets "${mockEnvironments[1].name}&page=1" as search param when user selected it`, async () => {
      const select = screen.getByRole("combobox", {
        name: filterLabel,
      });

      const option = screen.getByRole("option", {
        name: mockEnvironments[1].name,
      });

      await userEvent.selectOptions(select, option);

      await waitFor(() => {
        expect(window.location.search).toEqual(
          `?environment=${mockEnvironments[1].id}&page=1`
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

  describe("gives user information if fetching environments failed", () => {
    const testError: KlawApiError = {
      message: "Oh no, this did not work",
      success: false,
    };

    const originalConsoleError = jest.fn();

    beforeEach(async () => {
      console.error = jest.fn();
      mockGetEnvironments.mockRejectedValue(testError);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);

      customRender(<WrappedEnvironmentFilter />, {
        memoryRouter: true,
        queryClient: true,
        aquariumContext: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("async-select-loading-environments")
      );
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.resetAllMocks();
    });

    it("shows a disabled select with No Environments", () => {
      const select = screen.getByRole("combobox", {
        name: "No Environments",
      });

      expect(select).toBeDisabled();
    });

    it("shows an error message below the select element", () => {
      const errorText = screen.getByText("Environments could not be loaded.");

      expect(errorText).toBeVisible();
    });

    it("shows a toast notification with error", () => {
      expect(mockedUseToast).toHaveBeenCalledWith(
        expect.objectContaining({
          message: `Error loading Environments: ${testError.message}`,
        })
      );
      expect(console.error).toHaveBeenCalledWith(testError);
    });
  });
});
