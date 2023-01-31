import { cleanup, screen } from "@testing-library/react";
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

  beforeAll(() => {
    renderForm(<TopicNameField topicNames={mockedTopicNames} />, {
      schema,
      onSubmit,
      onError,
    });
  });

  afterAll(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders NativeSelect component", () => {
    const select = screen.getByRole("combobox");

    expect(select).toBeVisible();
    expect(select).toBeEnabled();
  });

  it("renders NativeSelect with a placeholder option", () => {
    const options = screen.getAllByRole("option");

    expect(options).toHaveLength(mockedTopicNames.length + 1);
  });

  it("renders disabled NativeSelect when topicNames is empty array", () => {
    cleanup();

    renderForm(<TopicNameField topicNames={[]} />, {
      schema,
      onSubmit,
      onError,
    });

    const select = screen.getByRole("combobox");

    expect(select).toBeDisabled();
  });
});
