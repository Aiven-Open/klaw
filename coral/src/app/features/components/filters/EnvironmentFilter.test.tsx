import { cleanup, screen, waitFor } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import {
  getAllEnvironmentsForTopicAndAcl,
  getAllEnvironmentsForSchema,
  getAllEnvironmentsForConnector,
} from "src/domain/environment";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/environment/environment-api.ts");

const mockGetEnvironments =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

const mockGetSchemaRegistryEnvironments =
  getAllEnvironmentsForSchema as jest.MockedFunction<
    typeof getAllEnvironmentsForSchema
  >;

const mockGetSyncConnectorsEnvironments =
  getAllEnvironmentsForConnector as jest.MockedFunction<
    typeof getAllEnvironmentsForConnector
  >;

const mockEnvironments = [
  createEnvironment({
    name: "DEV",
    id: "1",
  }),
  createEnvironment({
    name: "TST",
    id: "2",
  }),
];

const filterLabel = "Filter by Environment";

describe("EnvironmentFilter.tsx", () => {
  describe("uses a given endpoint to fetch environments", () => {
    beforeEach(() => {
      mockGetEnvironments.mockResolvedValue([]);
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);
    });
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("fetches from the getAllEnvironmentsForTopicAndAcl endpoint", () => {
      customRender(
        <EnvironmentFilter
          environmentEndpoint={"getAllEnvironmentsForTopicAndAcl"}
        />,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      expect(mockGetEnvironments).toHaveBeenCalled();
      expect(mockGetSchemaRegistryEnvironments).not.toHaveBeenCalled();
      expect(mockGetSyncConnectorsEnvironments).not.toHaveBeenCalled();
    });

    it("fetches from the getAllEnvironmentsForSchema endpoint", () => {
      customRender(
        <EnvironmentFilter
          environmentEndpoint={"getAllEnvironmentsForSchema"}
        />,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      expect(mockGetSchemaRegistryEnvironments).toHaveBeenCalled();
      expect(mockGetEnvironments).not.toHaveBeenCalled();
      expect(mockGetSyncConnectorsEnvironments).not.toHaveBeenCalled();
    });

    it("fetches from the getAllEnvironmentsForConnector endpoint", () => {
      customRender(
        <EnvironmentFilter
          environmentEndpoint={"getAllEnvironmentsForConnector"}
        />,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );

      expect(mockGetSyncConnectorsEnvironments).toHaveBeenCalled();
      expect(mockGetEnvironments).not.toHaveBeenCalled();
      expect(mockGetSchemaRegistryEnvironments).not.toHaveBeenCalled();
    });
  });

  describe("renders all necessary elements", () => {
    beforeAll(async () => {
      mockGetEnvironments.mockResolvedValue(mockEnvironments);
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);

      customRender(
        <EnvironmentFilter
          environmentEndpoint={"getAllEnvironmentsForTopicAndAcl"}
        />,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
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

  describe("sets the active environment based on a query param", () => {
    const mockedQueryParamDev = mockEnvironments[0].id;

    beforeEach(async () => {
      const routePath = `/?environment=${mockedQueryParamDev}`;

      mockGetEnvironments.mockResolvedValue(mockEnvironments);
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);

      customRender(
        <EnvironmentFilter
          environmentEndpoint={"getAllEnvironmentsForTopicAndAcl"}
        />,
        {
          memoryRouter: true,
          queryClient: true,
          customRoutePath: routePath,
        }
      );
      await waitForElementToBeRemoved(
        screen.getByTestId("select-environment-loading")
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
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);

      customRender(
        <EnvironmentFilter
          environmentEndpoint={"getAllEnvironmentsForTopicAndAcl"}
        />,
        {
          queryClient: true,
          memoryRouter: true,
        }
      );
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
        name: mockEnvironments[1].name,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(mockEnvironments[1].id);
    });
  });

  describe("updates the search param to preserve environment in url", () => {
    beforeEach(async () => {
      mockGetEnvironments.mockResolvedValue(mockEnvironments);
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
      mockGetSyncConnectorsEnvironments.mockResolvedValue([]);

      customRender(
        <EnvironmentFilter
          environmentEndpoint={"getAllEnvironmentsForTopicAndAcl"}
        />,
        {
          queryClient: true,
          browserRouter: true,
        }
      );
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
});
