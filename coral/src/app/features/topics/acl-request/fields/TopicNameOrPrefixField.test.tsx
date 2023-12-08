import { cleanup } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import TopicNameOrPrefixField from "src/app/features/topics/acl-request/fields/TopicNameOrPrefixField";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const producerSchema = z.object({
  aclPatternType: z.union([z.literal("LITERAL"), z.literal("PREFIXED")]),
});
const consumerSchema = z.object({
  aclPatternType: z.literal("LITERAL"),
});

const mockedTopicNames = ["topic-one", "topic-two", "topic-three"];
const producerLiteral = "LITERAL";
const producerPrefixed = "PREFIXED";
const consumerLiteral = "LITERAL";

describe("TopicNameOrPrefixField", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders a readOnly field with information when no pattern type is chosen", () => {
    const result = renderForm(
      <TopicNameOrPrefixField
        topicNames={mockedTopicNames}
        aclPatternType={undefined}
      />,
      {
        schema: producerSchema,
        onSubmit,
        onError,
      }
    );
    const field = result.getByRole("textbox");
    expect(field).toBeVisible();
    expect(field).toHaveAttribute("readonly");
    expect(field).toHaveValue(
      "Select environment and topic pattern type first"
    );
  });

  it("renders a TopicNameField (producer form)", async () => {
    const result = renderForm(
      <TopicNameOrPrefixField
        topicNames={mockedTopicNames}
        aclPatternType={producerLiteral}
      />,
      {
        schema: producerSchema,
        onSubmit,
        onError,
      }
    );
    const field = result.getByRole("combobox");
    await userEvent.click(field);
    const options = result.getAllByRole("option");

    expect(field).toBeVisible();
    expect(field).toBeEnabled();
    expect(options).toHaveLength(mockedTopicNames.length);
  });

  it("renders a readOnly TopicNameField (producer form)", () => {
    const result = renderForm(
      <TopicNameOrPrefixField
        topicNames={mockedTopicNames}
        aclPatternType={producerLiteral}
        readOnly={true}
      />,
      {
        schema: producerSchema,
        onSubmit,
        onError,
      }
    );
    const field = result.getByRole("combobox", {
      name: "Topic name (read-only)",
    });

    expect(field).toBeVisible();
    // disabled prop not forwarded to Combobox
    // expect(field).toBeDisabled();
    expect(field).toHaveAttribute("aria-readonly", "true");
  });

  it("renders a text field (producer form)", () => {
    const result = renderForm(
      <TopicNameOrPrefixField
        topicNames={mockedTopicNames}
        aclPatternType={producerPrefixed}
      />,
      {
        schema: producerSchema,
        onSubmit,
        onError,
      }
    );

    const input = result.getByRole("textbox");
    expect(input).toBeVisible();
    expect(input).toBeEnabled();
  });

  it("renders a TopicNameField (consumer form)", async () => {
    const result = renderForm(
      <TopicNameOrPrefixField
        topicNames={mockedTopicNames}
        aclPatternType={consumerLiteral}
      />,
      {
        schema: consumerSchema,
        onSubmit,
        onError,
      }
    );

    const field = result.getByRole("combobox");
    await userEvent.click(field);
    const options = result.getAllByRole("option");

    expect(field).toBeVisible();
    expect(field).toBeEnabled();
    expect(options).toHaveLength(mockedTopicNames.length);
  });
});
