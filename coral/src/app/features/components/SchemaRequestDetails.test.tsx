import { cleanup, render, screen, within } from "@testing-library/react";
import { SchemaRequestDetails } from "src/app/features/components/SchemaRequestDetails";
import { SchemaRequest } from "src/domain/schema-request";

const testRequest: SchemaRequest = {
  req_no: 1014,
  topicname: "testtopic-first",
  environment: "1",
  environmentName: "BRG",
  schemaversion: "1.0",
  teamname: "NCC1701D",
  teamId: 1701,
  appname: "App",
  schemafull: "",
  requestor: "jlpicard",
  requesttime: "1987-09-28T13:37:00.001+00:00",
  requesttimestring: "28-Sep-1987 13:37:00",
  requestStatus: "CREATED",
  requestOperationType: "CREATE",
  remarks: "asap",
  approvingTeamDetails:
    "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
  approvingtime: "2022-11-04T14:54:13.414+00:00",
  totalNoPages: "4",
  allPageNos: ["1"],
  currentPage: "1",
  deletable: false,
  editable: false,
  forceRegister: false,
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

describe("SchemaRequestDetails", () => {
  describe("renders all necessary elements", () => {
    beforeAll(() => {
      render(<SchemaRequestDetails request={testRequest} />);
    });

    afterAll(cleanup);

    it("shows the Environment", () => {
      const term = findTerm("Environment");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.environmentName);
    });

    it("shows the Topic name", () => {
      const term = findTerm("Topic name");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.topicname);
    });

    it("shows the Schema version", () => {
      const term = findTerm("Schema version");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.schemaversion as string);
    });

    it("shows a preview of the schema", () => {
      const term = findTerm("Schema preview");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.schemafull);
    });

    it("shows a message to approver", () => {
      const term = findTerm("Message for the approver");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.remarks as string);
    });

    it("shows who requested the schema", () => {
      const term = findTerm("Requested by");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.requestor);
    });

    it("shows the time the schema request was made", () => {
      const term = findTerm("Requested on");
      const definition = findDefinition(term);

      expect(term).toBeVisible();
      expect(definition).toHaveTextContent(testRequest.requesttimestring);
    });
  });
});
