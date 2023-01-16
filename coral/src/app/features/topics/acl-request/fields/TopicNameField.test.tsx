import { cleanup } from "@testing-library/react";
import TopicNameField from "src/app/features/topics/acl-request/fields/TopicNameField";
import { topicname } from "src/app/features/topics/acl-request/schemas/topic-acl-request-shared-fields";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const schema = z.object({
  topicname,
});

const mockedTopicNames = ["topic-one", "topic-two", "topic-three"];

describe("TopicNameField", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders NativeSelect component", () => {
    const result = renderForm(
      <TopicNameField topicNames={mockedTopicNames} />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const select = result.getByRole("combobox");

    expect(select).toBeVisible();
    expect(select).toBeEnabled();
  });

  it("renders NativeSelect with a placeholder option", () => {
    const result = renderForm(
      <TopicNameField topicNames={mockedTopicNames} />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const options = result.getAllByRole("option");

    expect(options).toHaveLength(mockedTopicNames.length + 1);
  });
});
