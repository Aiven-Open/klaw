import { cleanup, screen, within } from "@testing-library/react";
import TopicNameField from "src/app/features/topics/acl-request/fields/TopicNameField";
import { topicname } from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-shared-fields";
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

  it("renders a required NativeSelect component", () => {
    renderForm(<TopicNameField topicNames={mockedTopicNames} />, {
      schema,
      onSubmit,
      onError,
    });
    const select = screen.getByRole("combobox", { name: "Topic name *" });

    expect(select).toBeEnabled();
    expect(select).toBeRequired();
  });

  it("renders NativeSelect with a placeholder option", () => {
    renderForm(<TopicNameField topicNames={mockedTopicNames} />, {
      schema,
      onSubmit,
      onError,
    });
    const select = screen.getByRole("combobox", { name: "Topic name *" });
    const options = within(select).getAllByRole("option");

    expect(options).toHaveLength(mockedTopicNames.length + 1);
  });

  it("renders a readOnly NativeSelect when readOnly prop is true", () => {
    renderForm(
      <TopicNameField topicNames={mockedTopicNames} readOnly={true} />,
      {
        schema,
        onSubmit,
        onError,
      }
    );

    const select = screen.getByRole("combobox", {
      name: "Topic name (read-only)",
    });

    expect(select).toBeDisabled();
    expect(select).toHaveAttribute("aria-readonly", "true");
  });
});
