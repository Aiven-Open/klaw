import { cleanup, screen } from "@testing-library/react";
import { ConnectorOverview } from "src/app/features/connectors/details/overview/ConnectorOverview";
import { ConnectorOverview as ConnectorOverviewType } from "src/domain/connector";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { within } from "@testing-library/react/pure";

const mockedUseConnectorDetails = jest.fn();
jest.mock("src/app/features/connectors/details/ConnectorDetails", () => ({
  useConnectorDetails: () => mockedUseConnectorDetails(),
}));

const connectorInfoPromotion: ConnectorOverviewType["connectorInfo"] = {
  connectorStatus: "",
  hasOpenClaimRequest: false,
  hasOpenRequest: false,
  highestEnv: false,
  connectorOwner: true,
  connectorId: 1003,
  connectorName: "release240",
  runningTasks: 0,
  failedTasks: 0,
  environmentId: "4",
  teamName: "Ospo",
  teamId: 0,
  showEditConnector: true,
  showDeleteConnector: true,
  connectorDeletable: true,
  connectorConfig:
    '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "release240",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
  environmentName: "DEV",
};

const connectorInfoNoPromotionOpenRequest: ConnectorOverviewType["connectorInfo"] =
  {
    ...connectorInfoPromotion,
    hasOpenRequest: true,
  };

const connectorInfoNotConnectorOwner: ConnectorOverviewType["connectorInfo"] = {
  ...connectorInfoPromotion,
  connectorOwner: false,
};

const testConnectorOverview: ConnectorOverviewType = {
  connectorHistoryList: [],
  promotionDetails: {
    sourceEnv: "4",
    connectorName: "release240",
    targetEnvId: "10",
    sourceConnectorConfig:
      '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "release240",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
    targetEnv: "TST",
    status: "SUCCESS",
  },
  connectorExists: true,
  availableEnvironments: [
    {
      id: "4",
      name: "DEV",
    },
  ],
  connectorIdForDocumentation: 1003,
  connectorInfo: connectorInfoNotConnectorOwner,
};

describe("ConnectorOverview", () => {
  describe("renders right view for user that is not connector owner", () => {
    beforeAll(() => {
      mockedUseConnectorDetails.mockReturnValue({
        environmentId: "4",
        connectorOverview: {
          ...testConnectorOverview,
          connectorInfo: connectorInfoNotConnectorOwner,
        },
      });
      customRender(<ConnectorOverview />, { memoryRouter: true });
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows the correct header", () => {
      const header = screen.getByText("Connector configuration");

      expect(header).toBeVisible();
    });

    it("shows an editor with preview of the connector info", () => {
      const previewEditor = screen.getByTestId("connector-editor");

      expect(previewEditor).toBeVisible();
      expect(previewEditor).toHaveTextContent(
        `"connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector"`
      );
    });
  });

  describe("renders right view for connector owner", () => {
    describe("shows all necessary elements", () => {
      beforeAll(() => {
        mockedUseConnectorDetails.mockReturnValue({
          environmentId: "4",
          connectorOverview: {
            ...testConnectorOverview,
          },
        });
        customRender(<ConnectorOverview />, { memoryRouter: true });
      });

      afterAll(() => {
        cleanup();
        jest.clearAllMocks();
      });

      it("shows the correct header", () => {
        const header = screen.getByText("Connector configuration");

        expect(header).toBeVisible();
      });

      it("shows an editor with preview of the connector info", () => {
        const previewEditor = screen.getByTestId("connector-editor");

        expect(previewEditor).toBeVisible();
        expect(previewEditor).toHaveTextContent(
          `"connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector"`
        );
      });
    });

    describe("shows when promotion is possible", () => {
      beforeAll(() => {
        mockedUseConnectorDetails.mockReturnValue({
          environmentId: "4",
          connectorOverview: {
            ...testConnectorOverview,
            connectorInfo: connectorInfoPromotion,
          },
        });
        customRender(<ConnectorOverview />, { memoryRouter: true });
      });

      afterAll(() => {
        cleanup();
        jest.clearAllMocks();
      });

      it("shows a banner with information that promotion is possible", () => {
        const banner = screen.getByTestId("connector-promotion-banner");

        expect(banner).toBeVisible();
        expect(banner).toHaveTextContent(
          `This connector has not yet been promoted to the ${testConnectorOverview.promotionDetails.targetEnv} environment`
        );
      });

      it("shows a link to the promotion form", () => {
        const banner = screen.getByTestId("connector-promotion-banner");
        const link = within(banner).getByRole("link", { name: "Promote" });

        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          `/connector/${connectorInfoPromotion.connectorName}/request-promotion?sourceEnv=${testConnectorOverview.promotionDetails.sourceEnv}&targetEnv=${testConnectorOverview.promotionDetails.targetEnvId}`
        );
      });
    });

    describe("shows when promotion is not possible due to a pending request", () => {
      beforeAll(() => {
        mockedUseConnectorDetails.mockReturnValue({
          environmentId: "4",
          connectorOverview: {
            ...testConnectorOverview,
            connectorInfo: connectorInfoNoPromotionOpenRequest,
          },
        });
        customRender(<ConnectorOverview />, { memoryRouter: true });
      });

      afterAll(() => {
        cleanup();
        jest.clearAllMocks();
      });

      it("shows a banner with information that promotion is possible", () => {
        const banner = screen.getByTestId("connector-promotion-banner");

        expect(banner).toBeVisible();
        expect(banner).toHaveTextContent(
          `You cannot promote the connector at this time. ${connectorInfoNoPromotionOpenRequest.connectorName} has a pending request`
        );
      });

      it("shows a link to the pending request", () => {
        const banner = screen.getByTestId("connector-promotion-banner");
        const link = within(banner).getByRole("link", { name: "View request" });

        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          `/requests/connectors?search=${connectorInfoNoPromotionOpenRequest.connectorName}&requestType=ALL&status=CREATED&page=1`
        );
      });
    });
  });
});
