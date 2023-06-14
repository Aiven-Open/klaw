import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen } from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { TopicDetailsSchema } from "src/app/features/topics/details/schema/TopicDetailsSchema";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedUseTopicDetails = jest.fn();
jest.mock("src/app/features/topics/details/TopicDetails", () => ({
  useTopicDetails: () => mockedUseTopicDetails(),
}));
const mockSetSchemaVersion = jest.fn();

const testTopicName = "topic-name";
const testEnvironmentId = 1;
const testTopicSchemas = {
  topicExists: true,
  schemaExists: true,
  prefixAclsExists: false,
  txnAclsExists: false,
  allSchemaVersions: [3, 2, 1],
  latestVersion: 3,
  schemaPromotionDetails: {
    DEV: {
      status: "NO_PROMOTION",
      sourceEnv: "1",
      targetEnv: "TST",
      targetEnvId: "2",
    },
  },
  schemaDetailsPerEnv: {
    id: 0,
    version: 3,
    nextVersion: 2,
    prevVersion: 0,
    compatibility: "Couldn't retrieve",
    content:
      '{\n  "type" : "record",\n  "name" : "klawTestAvro",\n  "namespace" : "klaw.avro",\n  "fields" : [ {\n    "name" : "producer",\n    "type" : "string",\n    "doc" : "Name of the producer"\n  }, {\n    "name" : "body",\n    "type" : "string",\n    "doc" : "The body of the message being sent."\n  }, {\n    "name" : "timestamp",\n    "type" : "long",\n    "doc" : "time in seconds from epoc when the message was created."\n  } ],\n  "doc:" : "A basic schema for testing klaw - this is V3"\n}',
    env: "DEV",
    showNext: true,
    showPrev: false,
    latest: true,
  },
};

describe("TopicDetailsSchema (topic owner)", () => {
  beforeAll(() => {
    mockedUseTopicDetails.mockReturnValue({
      topicName: testTopicName,
      environmentId: testEnvironmentId,
      topicSchemas: testTopicSchemas,
      setSchemaVersion: mockSetSchemaVersion,
      topicOverview: { topicInfoList: [{ topicOwner: true }] },
    });
    customRender(
      <AquariumContext>
        <TopicDetailsSchema />{" "}
      </AquariumContext>,
      {
        memoryRouter: true,
        queryClient: true,
      }
    );
  });

  afterAll(() => {
    cleanup();
    jest.clearAllMocks();
  });

  it("shows a headline", () => {
    const headline = screen.getByRole("heading", { name: "Schema" });

    expect(headline).toBeVisible();
  });

  it("shows a select element to choose version", () => {
    const select = screen.getByRole("combobox", { name: "Select version" });

    expect(select).toBeEnabled();
  });

  it("shows all options", () => {
    const select = screen.getByRole("combobox", { name: "Select version" });
    const options = within(select).getAllByRole("option");

    expect(options).toHaveLength(3);
    expect(options[0]).toHaveValue("3");
    expect(options[0]).toHaveTextContent("Version 3 (latest)");
  });

  it("shows information about available amount of versions", () => {
    const versions = screen.getByText("3 versions");

    expect(versions).toBeVisible();
  });

  it("shows a link to request a new schema version", () => {
    const link = screen.getByRole("link", { name: "Request a new version" });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/topic/${testTopicName}/request-schema?env=${testTopicSchemas.schemaDetailsPerEnv.env}`
    );
  });

  it("shows information about possible promotion", () => {
    const infoText = screen.getByText(
      `This schema has not yet been promoted to the ${testTopicSchemas.schemaPromotionDetails["DEV"].targetEnv} environment.`
    );

    expect(infoText).toBeVisible();
  });

  it("shows a button to promote schema", () => {
    const button = screen.getByRole("button", { name: "Promote" });

    expect(button).toBeEnabled();
  });

  it("shows information about schema promotion", () => {
    const banner = screen.getByText("This schema has not yet been promoted", {
      exact: false,
    });
    const button = screen.getByRole("button", { name: "Promote" });

    expect(banner).toBeVisible();
    expect(button).toBeEnabled();
  });

  it("shows schema statistic about versions", () => {
    const versionsStats = screen.getByText("Version no.");

    expect(versionsStats).toBeVisible();
    expect(versionsStats.parentElement).toHaveTextContent("3Version no.");
  });

  it("shows schema info about ID", () => {
    const idInfo = screen.getByText("ID");

    expect(idInfo).toBeVisible();
    expect(idInfo.parentElement).toHaveTextContent("0ID");
  });

  it("shows schema info about compatibility", () => {
    const compatibilityInfo = screen.getByText("Compatibility");

    expect(compatibilityInfo).toBeVisible();
    expect(compatibilityInfo.parentElement).toHaveTextContent(
      "COULDN'T RETRIEVECompatibility"
    );
  });

  it("shows an editor with preview of the schema", () => {
    const previewEditor = screen.getByTestId("topic-schema");

    expect(previewEditor).toBeVisible();
  });

  it("allows changing the version of the schema", async () => {
    const select = screen.getByRole("combobox", { name: "Select version" });
    await userEvent.selectOptions(select, "2");

    expect(select).toHaveValue("2");

    expect(mockSetSchemaVersion).toHaveBeenCalledWith(2);
  });
});

describe("TopicDetailsSchema (NOT topic owner)", () => {
  beforeAll(() => {
    mockedUseTopicDetails.mockReturnValue({
      topicName: testTopicName,
      environmentId: testEnvironmentId,
      topicSchemas: testTopicSchemas,
      setSchemaVersion: mockSetSchemaVersion,
      topicOverview: { topicInfoList: [{ topicOwner: false }] },
    });
    customRender(
      <AquariumContext>
        <TopicDetailsSchema />{" "}
      </AquariumContext>,
      {
        memoryRouter: true,
        queryClient: true,
      }
    );
  });

  afterAll(cleanup);

  it("does not show a link to request a new schema version", () => {
    const link = screen.queryByRole("link", {
      name: "Request a new version",
    });

    expect(link).not.toBeInTheDocument();
  });

  it("does not show information about schema promotion", () => {
    const banner = screen.queryByText("This schema has not yet been promoted", {
      exact: false,
    });
    const button = screen.queryByRole("button", { name: "Promote" });

    expect(banner).not.toBeInTheDocument();
    expect(button).not.toBeInTheDocument();
  });
});
