import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import EnvironmentsTabs from "src/app/features/configuration/environments/components/EnvironmentsTabs";
import {
  ENVIRONMENT_TAB_ID_INTO_PATH,
  EnvironmentsTabEnum,
  Routes,
} from "src/app/router_utils";
import {
  getPaginatedEnvironmentsForConnector,
  getPaginatedEnvironmentsForSchema,
  getPaginatedEnvironmentsForTopicAndAcl,
} from "src/domain/environment/environment-api";
import { EnvironmentPaginatedApiResponse } from "src/domain/environment/environment-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const mockedKafkaTotalEnvs: EnvironmentPaginatedApiResponse = {
  totalPages: 1,
  currentPage: 1,
  totalEnvs: 1,
  entries: [
    {
      type: "kafka",
      name: "DEV",
      id: "1001",
      params: {},
    },
  ],
};

const mockedSchemaTotalEnvs: EnvironmentPaginatedApiResponse = {
  totalPages: 1,
  currentPage: 1,
  totalEnvs: 2,
  entries: [
    {
      type: "schemaregistry",
      name: "DEV",
      id: "1001",
      params: {},
    },
    {
      type: "schemaregistry",
      name: "TST",
      id: "1002",
      params: {},
    },
  ],
};

const mockedConnectorTotalEnvs: EnvironmentPaginatedApiResponse = {
  totalPages: 0,
  currentPage: 0,
  totalEnvs: 0,
  entries: [],
};

jest.mock("src/domain/environment/environment-api.ts");

const mockGetPaginatedEnvironmentsForTopicAndAcl =
  getPaginatedEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForTopicAndAcl
  >;
const mockGetPaginatedEnvironmentsForSchema =
  getPaginatedEnvironmentsForSchema as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForSchema
  >;
const mockGetPaginatedEnvironmentsForConnector =
  getPaginatedEnvironmentsForConnector as jest.MockedFunction<
    typeof getPaginatedEnvironmentsForConnector
  >;

describe("EnvironmentsTabs", () => {
  let user: ReturnType<typeof userEvent.setup>;

  describe("Tab badges", () => {
    beforeEach(() => {
      mockGetPaginatedEnvironmentsForTopicAndAcl.mockResolvedValue(
        mockedKafkaTotalEnvs
      );
      mockGetPaginatedEnvironmentsForSchema.mockResolvedValue(
        mockedSchemaTotalEnvs
      );
      mockGetPaginatedEnvironmentsForConnector.mockResolvedValue(
        mockedConnectorTotalEnvs
      );

      customRender(
        <EnvironmentsTabs currentTab={EnvironmentsTabEnum.KAFKA} />,
        {
          queryClient: true,
          memoryRouter: true,
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();

      cleanup();
    });

    it("renders correctenvironments count for Kafka", async () => {
      await screen.findByRole("tab", {
        name: "Kafka, 1 environment",
      });
    });

    it("renders correctenvironments count for Schema Registry", async () => {
      await screen.findByRole("tab", {
        name: "Schema Registry, 2 environments",
      });
    });

    it("renders correctenvironments count for Kafka Connect", async () => {
      await screen.findByRole("tab", {
        name: "Kafka Connect, no environments",
      });
    });
  });

  describe("Tab navigation", () => {
    beforeEach(() => {
      user = userEvent.setup();
      mockGetPaginatedEnvironmentsForTopicAndAcl.mockResolvedValue(
        mockedKafkaTotalEnvs
      );
      mockGetPaginatedEnvironmentsForSchema.mockResolvedValue(
        mockedSchemaTotalEnvs
      );
      mockGetPaginatedEnvironmentsForConnector.mockResolvedValue(
        mockedConnectorTotalEnvs
      );

      customRender(
        <EnvironmentsTabs currentTab={EnvironmentsTabEnum.KAFKA} />,
        {
          queryClient: true,
          memoryRouter: true,
        }
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it('navigates to correct URL when "Kafka" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "Kafka" }));
      expect(mockedNavigate).toHaveBeenCalledWith(
        `${Routes.ENVIRONMENTS}/${
          ENVIRONMENT_TAB_ID_INTO_PATH[EnvironmentsTabEnum.KAFKA]
        }`
      );
    });

    it('navigates to correct URL when "Schema Registry" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "Schema Registry" }));
      expect(mockedNavigate).toHaveBeenCalledWith(
        `${Routes.ENVIRONMENTS}/${
          ENVIRONMENT_TAB_ID_INTO_PATH[EnvironmentsTabEnum.SCHEMA_REGISTRY]
        }`
      );
    });

    it('navigates to correct URL when "Kafka Connect" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "Kafka Connect" }));
      expect(mockedNavigate).toHaveBeenCalledWith(
        `${Routes.ENVIRONMENTS}/${
          ENVIRONMENT_TAB_ID_INTO_PATH[EnvironmentsTabEnum.KAFKA_CONNECT]
        }`
      );
    });
  });
});
