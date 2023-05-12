import { cleanup, screen } from "@testing-library/react";
import TopicNameField from "src/app/features/topics/acl-request/fields/TopicNameField";
import { topicname } from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-shared-fields";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const schema = z.object({
  topicname,
});

const mockedTopicNames = ["topic-one", "topic-two", "topic-three"];

describe("TopicNameField", () => {
  const onSubmit = vi.fn();
  const onError = vi.fn();

  beforeEach(() => {
    renderForm(<TopicNameField topicNames={mockedTopicNames} />, {
      schema,
      onSubmit,
      onError,
    });
  });

  afterEach(() => {
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

  it("renders readonly NativeSelect when readOnly prop is true", () => {
    cleanup();

    renderForm(
      <TopicNameField topicNames={mockedTopicNames} readOnly={true} />,
      {
        schema,
        onSubmit,
        onError,
      }
    );

    const select = screen.getByRole("combobox");

    expect(select).toBeDisabled();
    expect(select).toHaveAttribute("aria-readonly", "true");
  });
});
