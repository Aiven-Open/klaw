import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen } from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { TopicDetailsSchema } from "src/app/features/topics/details/schema/TopicDetailsSchema";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { TopicSchemaOverview } from "src/domain/topic";
import { promoteSchemaRequest } from "src/domain/schema-request";

jest.mock("src/domain/schema-request/schema-request-api.ts");
const mockPromoteSchemaRequest = promoteSchemaRequest as jest.MockedFunction<
  typeof promoteSchemaRequest
>;

const mockedUseTopicDetails = jest.fn();
jest.mock("src/app/features/topics/details/TopicDetails", () => ({
  useTopicDetails: () => mockedUseTopicDetails(),
}));
const mockSetSchemaVersion = jest.fn();

const testTopicName = "topic-name";
const testEnvironmentId = 1;
const testTopicSchemas: TopicSchemaOverview = {
  prefixAclsExists: false,
  txnAclsExists: false,
  topicExists: true,
  schemaExists: true,
  allSchemaVersions: [3, 2, 1],
  latestVersion: 3,
  schemaPromotionDetails: {
    status: "SUCCESS",
    sourceEnv: "1",
    targetEnv: "TST",
    targetEnvId: "2",
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

const noPromotion_testTopicSchemas = {
  topicExists: true,
  schemaExists: true,
  prefixAclsExists: false,
  txnAclsExists: false,
  allSchemaVersions: [3, 2, 1],
  latestVersion: 3,
  schemaPromotionDetails: {
    status: "NO_PROMOTION",
  },
  schemaDetailsPerEnv: {
    id: 0,
    version: 3,
    nextVersion: 2,
    prevVersion: 0,
    compatibility: "Couldn't retrieve",
    content:
      '{\n  "type" : "record",\n  "name" : "klawTestAvro",\n  "namespace" : "klaw.avro",\n  "fields" : [ {\n    "name" : "producer",\n    "type" : "string",\n    "doc" : "Name of the producer"\n  }, {\n    "name" : "body",\n    "type" : "string",\n    "doc" : "The body of the message being sent."\n  }, {\n    "name" : "timestamp",\n    "type" : "long",\n    "doc" : "time in seconds from epoc when the message was created."\n  } ],\n  "doc:" : "A basic schema for testing klaw - this is V3"\n}',
    env: "TST",
    showNext: true,
    showPrev: false,
    latest: true,
  },
};

describe("TopicDetailsSchema", () => {
  const user = userEvent.setup();

  describe("renders right view for topic owner", () => {
    beforeAll(() => {
      mockPromoteSchemaRequest.mockResolvedValue({
        success: true,
        message: "",
      });
      mockedUseTopicDetails.mockReturnValue({
        topicOverviewIsRefetching: false,
        topicSchemasIsRefetching: false,
        topicName: testTopicName,
        environmentId: testEnvironmentId,
        topicSchemas: testTopicSchemas,
        setSchemaVersion: mockSetSchemaVersion,
        topicOverview: { topicInfo: { topicOwner: true } },
      });
      customRender(
        <AquariumContext>
          <TopicDetailsSchema />
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
      const link = screen.getByRole("link", {
        name: "Request a new version",
      });

      // schemaDetailsPerEnv could be undefined based on its type,
      // but it's always defined for the test here
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      const environment = testTopicSchemas.schemaDetailsPerEnv.env;

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        `/topic/${testTopicName}/request-schema?env=${environment}`
      );
    });

    it("shows information about possible promotion", () => {
      const infoText = screen.getByText(
        `This schema has not yet been promoted to the ${testTopicSchemas.schemaPromotionDetails.targetEnv} environment.`
      );

      expect(infoText).toBeVisible();
    });

    it("shows a button to promote schema", () => {
      const button = screen.getByRole("button", { name: "Promote" });

      expect(button).toBeEnabled();
    });

    it("shows a modal to promote schema when clicking on the Promote schema button", async () => {
      const button = screen.getByRole("button", { name: "Promote" });

      await userEvent.click(button);

      expect(screen.getByRole("dialog")).toBeVisible();
    });

    it("shows information about schema promotion", () => {
      const promotionBanner = screen.getByTestId("schema-promotion-banner");
      const infoText = within(promotionBanner).getByText(
        "This schema has not yet been promoted",
        {
          exact: false,
        }
      );
      const button = within(promotionBanner).getByRole("button", {
        name: "Promote",
      });

      expect(promotionBanner).toBeVisible();
      expect(infoText).toBeVisible();
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
  });

  describe("enables topic owner to change the version of a schema", () => {
    beforeEach(() => {
      mockPromoteSchemaRequest.mockResolvedValue({
        success: true,
        message: "",
      });
      mockedUseTopicDetails.mockReturnValue({
        topicOverviewIsRefetching: false,
        topicSchemasIsRefetching: false,
        topicName: testTopicName,
        environmentId: testEnvironmentId,
        topicSchemas: testTopicSchemas,
        setSchemaVersion: mockSetSchemaVersion,
        topicOverview: { topicInfo: { topicOwner: true } },
      });
      customRender(
        <AquariumContext>
          <TopicDetailsSchema />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("allows changing the version of the schema", async () => {
      const select = screen.getByRole("combobox", { name: "Select version" });
      await userEvent.selectOptions(select, "2");

      expect(select).toHaveValue("2");

      expect(mockSetSchemaVersion).toHaveBeenCalledWith(2);
    });
  });

  describe("enables topic owner to promote a schema", () => {
    const originalConsoleError = console.error;

    beforeEach(() => {
      console.error = jest.fn();

      mockedUseTopicDetails.mockReturnValue({
        topicOverviewIsRefetching: false,
        topicSchemasIsRefetching: false,
        topicName: testTopicName,
        environmentId: testEnvironmentId,
        topicSchemas: testTopicSchemas,
        setSchemaVersion: mockSetSchemaVersion,
        topicOverview: { topicInfo: { topicOwner: true } },
      });

      customRender(
        <AquariumContext>
          <TopicDetailsSchema />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
      jest.clearAllMocks();
    });

    it("sends an update schema request", async () => {
      mockPromoteSchemaRequest.mockResolvedValue({
        success: true,
        message: "",
      });

      const buttonPromote = screen.getByRole("button", { name: "Promote" });

      await user.click(buttonPromote);

      const modal = screen.getByRole("dialog");
      const buttonRequest = within(modal).getByRole("button", {
        name: "Request schema promotion",
      });

      await user.click(buttonRequest);

      expect(mockPromoteSchemaRequest).toHaveBeenCalledWith({
        forceRegister: false,
        remarks: "",
        schemaVersion: "3",
        sourceEnvironment: "1",
        targetEnvironment: "2",
        topicName: "topic-name",
      });

      expect(console.error).not.toHaveBeenCalled();
    });

    it("shows an error if promotion did fail", async () => {
      mockPromoteSchemaRequest.mockRejectedValue({
        success: false,
        message: "Oh no",
      });

      const buttonPromote = screen.getByRole("button", { name: "Promote" });

      await user.click(buttonPromote);

      const modal = screen.getByRole("dialog");
      const buttonRequest = within(modal).getByRole("button", {
        name: "Request schema promotion",
      });

      await user.click(buttonRequest);

      const alert = screen.getByRole("alert");
      const errorMessage = within(alert).getByText("Oh no");

      expect(alert).toBeVisible();
      expect(errorMessage).toBeVisible();
      expect(console.error).toHaveBeenCalledWith({
        success: false,
        message: "Oh no",
      });
    });
  });

  describe("renders right view for topic owner when data is updating", () => {
    beforeAll(() => {
      mockedUseTopicDetails.mockReturnValue({
        topicOverviewIsRefetching: false,
        topicSchemasIsRefetching: true,
        topicName: testTopicName,
        environmentId: testEnvironmentId,
        topicSchemas: testTopicSchemas,
        setSchemaVersion: mockSetSchemaVersion,
        topicOverview: { topicInfo: { topicOwner: true } },
      });
      customRender(
        <AquariumContext>
          <TopicDetailsSchema />
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

    it("shows no select element to choose version", () => {
      const select = screen.queryByRole("combobox", { name: "Select version" });

      expect(select).not.toBeInTheDocument();
    });

    it("shows accessible information that versions are loading", () => {
      const loadingInformation = screen.getByText("Versions loading");

      expect(loadingInformation).toBeVisible();
      expect(loadingInformation).toHaveClass("visually-hidden");
    });

    it("shows no information about available amount of versions", () => {
      const versions = screen.queryByText("3 versions");

      expect(versions).not.toBeInTheDocument();
    });

    it("shows a no link to request a new schema version", () => {
      const link = screen.queryByRole("link", {
        name: "Request a new version",
      });

      expect(link).not.toBeInTheDocument();
    });

    it("shows no information about possible promotion", () => {
      const infoText = screen.queryByText(
        `This schema has not yet been promoted to the ${testTopicSchemas.schemaPromotionDetails.targetEnv} environment.`
      );

      expect(infoText).not.toBeInTheDocument();
    });

    it("shows no button to promote schema", () => {
      const button = screen.queryByRole("button", { name: "Promote" });

      expect(button).not.toBeInTheDocument();
    });

    it("shows schema statistic about versions", () => {
      const versionsStats = screen.getByText("Version no.");

      expect(versionsStats).toBeVisible();
      expect(versionsStats.parentElement).toHaveTextContent(
        "Loading informationVersion no."
      );
    });

    it("shows schema info about ID", () => {
      const idInfo = screen.getByText("ID");

      expect(idInfo).toBeVisible();
      expect(idInfo.parentElement).toHaveTextContent("Loading informationID");
    });

    it("shows schema info about compatibility", () => {
      const compatibilityInfo = screen.getByText("Compatibility");

      expect(compatibilityInfo).toBeVisible();
      expect(compatibilityInfo.parentElement).toHaveTextContent(
        "Loading informationCompatibility"
      );
    });

    it("shows no editor with preview of the schema", () => {
      const previewEditor = screen.queryByTestId("topic-schema");

      expect(previewEditor).not.toBeInTheDocument();
    });

    it("shows accessible loading information for preview", () => {
      const loadingPreview = screen.getByText("Loading schema preview");

      expect(loadingPreview).toBeVisible();
      expect(loadingPreview).toHaveClass("visually-hidden");
    });
  });

  describe("renders right view for user that is not topic owner", () => {
    beforeAll(() => {
      mockedUseTopicDetails.mockReturnValue({
        topicOverviewIsRefetching: false,
        topicSchemasIsRefetching: false,
        topicName: testTopicName,
        environmentId: testEnvironmentId,
        topicSchemas: testTopicSchemas,
        setSchemaVersion: mockSetSchemaVersion,
        topicOverview: { topicInfo: { topicOwner: false } },
      });
      customRender(
        <AquariumContext>
          <TopicDetailsSchema />
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

    it("does not show a link to request a new schema version", () => {
      const link = screen.queryByRole("link", {
        name: "Request a new version",
      });

      expect(link).not.toBeInTheDocument();
    });

    it("does not show information about schema promotion", () => {
      const promotionBanner = screen.queryByTestId("schema-promotion-banner");
      const button = screen.queryByRole("button", { name: "Promote" });

      expect(promotionBanner).not.toBeInTheDocument();
      expect(button).not.toBeInTheDocument();
    });

    describe("TopicDetailsSchema (status: NO_PROMOTION)", () => {
      beforeAll(() => {
        mockedUseTopicDetails.mockReturnValue({
          topicOverviewIsRefetching: false,
          topicSchemasIsRefetching: false,
          topicName: testTopicName,
          environmentId: testEnvironmentId,
          topicSchemas: noPromotion_testTopicSchemas,
          setSchemaVersion: mockSetSchemaVersion,
          topicOverview: { topicInfo: { topicOwner: true } },
        });
        customRender(
          <AquariumContext>
            <TopicDetailsSchema />
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

        expect(link).toBeInTheDocument();
      });

      it("does not show information about schema promotion", () => {
        const banner = screen.queryByText(
          "This schema has not yet been promoted",
          {
            exact: false,
          }
        );
        const button = screen.queryByRole("button", { name: "Promote" });

        expect(banner).not.toBeInTheDocument();
        expect(button).not.toBeInTheDocument();
      });
    });
  });
});
