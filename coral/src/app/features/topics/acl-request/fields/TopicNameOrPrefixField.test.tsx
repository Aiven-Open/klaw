import { cleanup } from "@testing-library/react";
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

  it("renders a TopicNameField (producer form)", () => {
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
    const options = result.getAllByRole("option");

    expect(field).toBeVisible();
    expect(field).toBeEnabled();
    expect(options).toHaveLength(mockedTopicNames.length + 1);
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

  it("renders a TopicNameField (consumer form)", () => {
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
    const options = result.getAllByRole("option");

    expect(field).toBeVisible();
    expect(field).toBeEnabled();
    expect(options).toHaveLength(mockedTopicNames.length + 1);
  });

  it("renders empty Box when no aclPatternType is passed", () => {
    const result = renderForm(
      <TopicNameOrPrefixField topicNames={mockedTopicNames} />,
      {
        schema: producerSchema,
        onSubmit,
        onError,
      }
    );
    const empty = result.getByTestId("empty");
    expect(empty).toBeVisible();
  });
});
