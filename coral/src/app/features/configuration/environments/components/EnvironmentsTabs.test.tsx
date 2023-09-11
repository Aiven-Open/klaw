import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import EnvironmentsTabs from "src/app/features/configuration/environments/components/EnvironmentsTabs";
import {
  ENVIRONMENT_TAB_ID_INTO_PATH,
  EnvironmentsTabEnum,
  Routes,
} from "src/app/router_utils";
import * as environmentsApi from "src/domain/environment/environment-api";
import { EnvironmentPaginatedApiResponse } from "src/domain/environment/environment-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

class Deferred<T> {
  public promise: Promise<T>;
  public resolve!: (value: T | PromiseLike<T>) => void;
  public reject!: (value: T | PromiseLike<T>) => void;
  constructor() {
    this.promise = new Promise((resolve, reject) => {
      this.resolve = resolve;
      this.reject = reject;
    });
  }
}

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

describe("EnvironmentsTabs", () => {
  let user: ReturnType<typeof userEvent.setup>;
  const getPaginatedEnvironmentsForTopicAndAclSpy = jest
    .spyOn(environmentsApi, "getPaginatedEnvironmentsForTopicAndAcl")
    .mockImplementation(() => {
      throw Error(
        "getPaginatedEnvironmentsForTopicAndAcl return must be mocked"
      );
    });
  const getPaginatedEnvironmentsForSchemaSpy = jest
    .spyOn(environmentsApi, "getPaginatedEnvironmentsForSchema")
    .mockImplementation(() => {
      throw Error("getPaginatedEnvironmentsForSchema return must be mocked");
    });
  const getPaginatedEnvironmentsForConnectorSpy = jest
    .spyOn(environmentsApi, "getPaginatedEnvironmentsForConnector")
    .mockImplementation(() => {
      throw Error("getPaginatedEnvironmentsForConnector return must be mocked");
    });

  afterAll(() => {
    getPaginatedEnvironmentsForTopicAndAclSpy.mockReset();
    getPaginatedEnvironmentsForSchemaSpy.mockReset();
    getPaginatedEnvironmentsForConnectorSpy.mockReset();
  });

  describe("Tab badges", () => {
    let manualKafka: Deferred<EnvironmentPaginatedApiResponse>;
    let manualSchema: Deferred<EnvironmentPaginatedApiResponse>;
    let manualConnector: Deferred<EnvironmentPaginatedApiResponse>;

    beforeAll(() => {
      manualKafka = new Deferred();
      manualSchema = new Deferred();
      manualConnector = new Deferred();
      getPaginatedEnvironmentsForTopicAndAclSpy.mockReturnValue(
        manualKafka.promise
      );
      getPaginatedEnvironmentsForSchemaSpy.mockReturnValue(
        manualSchema.promise
      );
      getPaginatedEnvironmentsForConnectorSpy.mockReturnValue(
        manualConnector.promise
      );
      customRender(
        <EnvironmentsTabs currentTab={EnvironmentsTabEnum.KAFKA} />,
        {
          queryClient: true,
          memoryRouter: true,
        }
      );
    });

    afterAll(() => {
      cleanup();
    });

    describe("while environment count requests are in flight", () => {
      it("renders a tab for Kafka", () => {
        screen.getByRole("tab", { name: "Kafka" });
      });

      it("renders a tab for Schema registry", () => {
        screen.getByRole("tab", { name: "Schema Registry" });
      });

      it("renders a tab for Kafka Connect", () => {
        screen.getByRole("tab", { name: "Kafka Connect" });
      });

      describe("when environment count requests resolve", () => {
        beforeAll(() => {
          manualKafka.resolve(mockedKafkaTotalEnvs);
          manualSchema.resolve(mockedSchemaTotalEnvs);
          manualConnector.resolve(mockedConnectorTotalEnvs);
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
    });
  });

  describe("Tab navigation", () => {
    beforeEach(() => {
      user = userEvent.setup();
      getPaginatedEnvironmentsForTopicAndAclSpy.mockResolvedValue(
        mockedKafkaTotalEnvs
      );
      getPaginatedEnvironmentsForSchemaSpy.mockResolvedValue(
        mockedSchemaTotalEnvs
      );
      getPaginatedEnvironmentsForConnectorSpy.mockResolvedValue(
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
      mockedNavigate.mockReset();
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
