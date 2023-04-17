import { cleanup, render, screen, within } from "@testing-library/react";
import { ConnectorRequestDetails } from "src/app/features/components/ConnectorDetailsModalContent";
import { ConnectorRequest } from "src/domain/connector";

const testRequest: ConnectorRequest = {
  environment: "4",
  environmentName: "DEV",
  requestor: "aindriul",
  teamId: 1003,
  teamname: "Ospo",
  requestOperationType: "DELETE",
  requestStatus: "CREATED",
  requesttime: "2023-04-06T08:04:39.783+00:00",
  requesttimestring: "06-Apr-2023 08:04:39",
  currentPage: "1",
  totalNoPages: "1",
  allPageNos: ["1"],
  approvingTeamDetails:
    "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,amathieu,roopek,miketest,harshini,mischa,",
  connectorName: "Mirjam-10",
  description: "Mirjam-10",
  connectorConfig:
    '{\n  "name" : "Mirjam-10",\n  "topic" : "testtopic",\n  "tasks.max" : "1",\n  "topics.regex" : "*",\n  "connector.class" : "io.confluent.connect.storage.tools.ConnectorSourceConnector"\n}',
  connectorId: 1026,
  deletable: false,
  editable: false,
  remarks: "Hello there",
};

const findTerm = (term: string) => {
  return screen
    .getAllByRole("term")
    .find((value) => value.textContent === term);
};

// since all our dt/dd pairs are always wrapped in a div
// this makes sure the right pairs relate to each other.
// this depends heavily on DOM structure, so it can be
// brittle in case we change that. In that case, the helper
// function has to be updated.
const findDefinition = (term: HTMLElement | undefined) => {
  if (!term) throw Error("term is null");
  const termParent = term.parentElement;
  if (!termParent) throw Error("term has no parent element");

  return within(termParent).getByRole("definition");
};

describe("ConnectorRequestDetailsModalContent", () => {
  describe("renders all necessary elements", () => {
    beforeAll(() => {
      render(<ConnectorRequestDetails request={testRequest} />);
    });

    afterAll(cleanup);

    it("shows the Environment", () => {
      const term = findTerm("Environment");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.environmentName);
    });

    it("shows the Topic name", () => {
      const term = findTerm("Connector name");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.connectorName);
    });

    it("shows the Description", () => {
      const term = findTerm("Description");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.description);
    });

    it("shows the connector configuration", () => {
      const term = findTerm("Connector configuration");
      const definition = findDefinition(term);

      expect(term).toBeVisible();

      // Doing a toHaveTextContent assertion here does not work because of minute formatting issues
      // We parse the stringified JSON to ensure integrity of data
      const parsedDefinition = JSON.parse(definition.textContent as string);
      const parsedConnectorConfig = JSON.parse(testRequest.connectorConfig);

      expect(parsedDefinition).toEqual(parsedConnectorConfig);
    });

    it("shows a message to approver", () => {
      const term = findTerm("Message for the approver");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.remarks as string);
    });

    it("shows who requested the connector", () => {
      const term = findTerm("Requested by");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.requestor);
    });

    it("shows the time the connector request was made", () => {
      const term = findTerm("Requested on");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.requesttimestring);
    });
  });
});
