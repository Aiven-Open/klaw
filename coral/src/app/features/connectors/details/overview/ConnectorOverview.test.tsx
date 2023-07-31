import { render, screen } from "@testing-library/react";
import { ConnectorOverview } from "src/app/features/connectors/details/overview/ConnectorOverview";

const mockedUseConnectorDetails = jest.fn();
jest.mock("src/app/features/connectors/details/ConnectorDetails", () => ({
  useConnectorDetails: () => mockedUseConnectorDetails(),
}));

const testConnectorDetails = {
  environmentId: "4",
  connectorOverview: {
    topicHistoryList: [],
    promotionDetails: {
      sourceEnv: "4",
      connectorName: "release240",
      targetEnvId: "10",
      sourceConnectorConfig:
        '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "release240",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
      targetEnv: "TST",
      status: "success",
    },
    connectorExists: true,
    availableEnvironments: [
      {
        id: "4",
        name: "DEV",
      },
    ],
    topicIdForDocumentation: 1003,
    connectorInfo: {
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
    },
  },
};

describe("ConnectorOverview", () => {
  beforeAll(() => {
    mockedUseConnectorDetails.mockReturnValue(testConnectorDetails);
    render(<ConnectorOverview />);
  });

  it("shows the correct header", () => {
    const header = screen.getByText("Connector configuration");

    expect(header).toBeVisible();
  });

  it("shows an editor with preview of the connector info", () => {
    const previewEditor = screen.getByTestId("topic-connector");

    expect(previewEditor).toBeVisible();
    expect(previewEditor).toHaveTextContent(
      `"connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector"`
    );
  });
});
