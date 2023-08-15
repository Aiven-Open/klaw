import { cleanup, render, screen } from "@testing-library/react";
import TopicDetailsModalContent from "src/app/features/components/TopicDetailsModalContent";
import { TopicRequest } from "src/domain/topic/topic-types";

const noAdvancedConfigRequest: TopicRequest = {
  requestOperationType: "CREATE",
  description: "Hello there",
  remarks: "hello",
  topicid: 1015,
  topicpartitions: 2,
  replicationfactor: "4",
  topicname: "newaudittopic",
  environment: "2",
  teamname: "Ospo",
  environmentName: "TST",
  teamId: 1003,
  appname: "App",
  requestor: "amathieu",
  requesttime: "2023-01-10T13:19:10.757+00:00",
  requesttimestring: "10-Jan-2023 13:19:10",
  requestStatus: "CREATED",
  approver: undefined,
  approvingtime: undefined,
  currentPage: "1",
  otherParams: undefined,
  totalNoPages: "2",
  allPageNos: ["1", ">", ">>"],
  approvingTeamDetails:
    "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,aindriul,",
  editable: false,
  deletable: false,
  advancedTopicConfigEntries: [],
  deleteAssociatedSchema: false,
};

const withAdvancedConfigRequest: TopicRequest = {
  requestOperationType: "CREATE",
  description: "This topic description is mandatory!",
  remarks: undefined,
  topicid: 1015,
  topicpartitions: 2,
  replicationfactor: "4",
  topicname: "newaudittopic",
  environment: "2",
  teamname: "Ospo",
  environmentName: "TST",
  teamId: 1003,
  appname: "App",
  requestor: "amathieu",
  requesttime: "2023-01-10T13:19:10.757+00:00",
  requesttimestring: "10-Jan-2023 13:19:10",
  requestStatus: "CREATED",
  approver: undefined,
  approvingtime: undefined,
  currentPage: "1",
  otherParams: undefined,
  totalNoPages: "2",
  allPageNos: ["1", ">", ">>"],
  approvingTeamDetails:
    "Team : Ospo, Users : muralibasani,josepprat,samulisuortti,mirjamaulbach,smustafa,aindriul,",
  editable: false,
  deletable: false,
  advancedTopicConfigEntries: [{ configKey: "hello", configValue: "yes" }],
  deleteAssociatedSchema: false,
};

const findDefinition = (term?: string) => {
  return screen.getAllByRole("definition").find((value) => {
    return value.textContent === term;
  });
};

const findTerm = (term: string) => {
  return screen
    .getAllByRole("term")
    .find((value) => value.textContent === term);
};

describe("TopicDetailsModalContent", () => {
  describe("renders correct content for Topic request (description, remarks, no config)", () => {
    beforeAll(() => {
      render(
        <TopicDetailsModalContent topicRequest={noAdvancedConfigRequest} />
      );
    });
    afterAll(cleanup);

    it("renders Environment", () => {
      expect(findTerm("Environment")).toBeVisible();
      expect(findDefinition(noAdvancedConfigRequest.environment)).toBeVisible();
    });
    it("renders Request type", () => {
      expect(findTerm("Request type")).toBeVisible();
      expect(
        findDefinition(noAdvancedConfigRequest.requestOperationType)
      ).toBeVisible();
    });
    it("renders Topic", () => {
      expect(findTerm("Topic")).toBeVisible();
      expect(findDefinition(noAdvancedConfigRequest.topicname)).toBeVisible();
    });
    it("renders Topic description", () => {
      expect(findTerm("Topic description")).toBeVisible();
      expect(findDefinition(noAdvancedConfigRequest.description)).toBeVisible();
    });
    it("renders Topic partition", () => {
      expect(findTerm("Topic partition")).toBeVisible();
      expect(
        findDefinition(String(noAdvancedConfigRequest.topicpartitions))
      ).toBeVisible();
    });
    it("renders Topic replication factor", () => {
      expect(findTerm("Topic replication factor")).toBeVisible();
      expect(
        findDefinition(noAdvancedConfigRequest.replicationfactor)
      ).toBeVisible();
    });
    it("renders Message for approval", () => {
      expect(findTerm("Message for approval")).toBeVisible();
      expect(findDefinition(noAdvancedConfigRequest.remarks)).toBeVisible();
    });
    it("renders Requested by", () => {
      expect(findTerm("Requested by")).toBeVisible();
      expect(findDefinition(noAdvancedConfigRequest.requestor)).toBeVisible();
    });
    it("renders Requested on", () => {
      expect(findTerm("Requested on")).toBeVisible();
      expect(
        findDefinition(`${noAdvancedConfigRequest.requesttimestring} UTC`)
      ).toBeVisible();
    });
  });

  describe("renders correct content for Topic request (no description,  no remarks, with config)", () => {
    beforeAll(() => {
      render(
        <TopicDetailsModalContent topicRequest={withAdvancedConfigRequest} />
      );
    });
    afterAll(cleanup);

    it("renders Environment", () => {
      expect(findTerm("Environment")).toBeVisible();
      expect(findDefinition(noAdvancedConfigRequest.environment)).toBeVisible();
    });
    it("renders Request type", () => {
      expect(findTerm("Request type")).toBeVisible();
      expect(
        findDefinition(noAdvancedConfigRequest.requestOperationType)
      ).toBeVisible();
    });
    it("renders Topic", () => {
      expect(findTerm("Topic")).toBeVisible();
      expect(findDefinition(noAdvancedConfigRequest.topicname)).toBeVisible();
    });
    it("renders Topic description", () => {
      expect(findTerm("Topic description")).toBeVisible();
      expect(
        findDefinition("This topic description is mandatory!")
      ).toBeVisible();
    });
    it("renders Topic partition", () => {
      expect(findTerm("Topic partition")).toBeVisible();
      expect(
        findDefinition(String(noAdvancedConfigRequest.topicpartitions))
      ).toBeVisible();
    });
    it("renders Topic replication factor", () => {
      expect(findTerm("Topic replication factor")).toBeVisible();
      expect(
        findDefinition(noAdvancedConfigRequest.replicationfactor)
      ).toBeVisible();
    });
    it("renders Message for approval", () => {
      expect(findTerm("Message for approval")).toBeVisible();
      expect(findDefinition("No message")).toBeVisible();
    });
    it("renders Requested by", () => {
      expect(findTerm("Requested by")).toBeVisible();
      expect(findDefinition(noAdvancedConfigRequest.requestor)).toBeVisible();
    });
    it("renders Requested on", () => {
      expect(findTerm("Requested on")).toBeVisible();
      expect(
        findDefinition(`${noAdvancedConfigRequest.requesttimestring} UTC`)
      ).toBeVisible();
    });
    it("renders Advanced configuration", () => {
      expect(findTerm("Advanced configuration")).toBeVisible();
      expect(screen.getByTestId("topic-advanced-config")).toBeVisible();
    });
  });
});
