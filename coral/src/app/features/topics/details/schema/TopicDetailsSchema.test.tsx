import { cleanup, render, screen } from "@testing-library/react";
import { TopicDetailsSchema } from "src/app/features/topics/details/schema/TopicDetailsSchema";
import { within } from "@testing-library/react/pure";

const mockedUseTopicDetails = jest.fn();
jest.mock("src/app/features/topics/details/TopicDetails", () => ({
  useTopicDetails: () => mockedUseTopicDetails(),
}));

describe("TopicDetailsSchema", () => {
  const testTopicName = "topic-name";
  const testEnvironmentId = 1;
  beforeAll(() => {
    mockedUseTopicDetails.mockReturnValue({
      topicName: testTopicName,
      environmentId: testEnvironmentId,
    });
    render(<TopicDetailsSchema />);
  });

  afterAll(cleanup);

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

    expect(options).toHaveLength(1);
    expect(options[0]).toHaveValue("1");
    expect(options[0]).toHaveTextContent("version 1");
  });

  it("shows information about available amount of versions", () => {
    const versions = screen.getByText("5 versions");

    expect(versions).toBeVisible();
  });

  it("shows a link to request a new schema version", () => {
    const link = screen.getByRole("link", { name: "Request a new version" });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/topic/${testTopicName}/request-schema?env=${testEnvironmentId}`
    );
  });

  it("shows information about schema promotion", () => {
    const banner = screen.getByText("This schema has not yet been promoted", {
      exact: false,
    });

    expect(banner).toBeVisible();
  });

  it("shows schema statistic about versions", () => {
    const versionsStats = screen.getByText("Version no.");

    expect(versionsStats).toBeVisible();
    expect(versionsStats.parentElement).toHaveTextContent("99Version no.");
  });

  it("shows schema info about ID", () => {
    const idInfo = screen.getByText("ID");

    expect(idInfo).toBeVisible();
    expect(idInfo.parentElement).toHaveTextContent("999ID");
  });

  it("shows schema info about compatibility", () => {
    const compatibilityInfo = screen.getByText("Compatibility");

    expect(compatibilityInfo).toBeVisible();
    expect(compatibilityInfo.parentElement).toHaveTextContent(
      "BACKWARDSCompatibility"
    );
  });

  it("shows an editor with preview of the schema", () => {
    const previewEditor = screen.getByTestId("topic-schema");

    expect(previewEditor).toBeVisible();
  });
});
