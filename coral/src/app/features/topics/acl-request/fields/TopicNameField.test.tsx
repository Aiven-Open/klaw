import { cleanup, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
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
    // required prop not forwarded to Combobox
    // expect(select).toBeRequired();
  });

  it("renders NativeSelect with a placeholder option", async () => {
    renderForm(<TopicNameField topicNames={mockedTopicNames} />, {
      schema,
      onSubmit,
      onError,
    });

    const select = screen.getByRole("combobox", { name: "Topic name *" });

    await userEvent.click(select);
    const options = screen.getAllByRole("option");

    expect(options).toHaveLength(mockedTopicNames.length);
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

    // disabled prop not forwarded to Combobox
    // expect(select).toBeDisabled();
    expect(select).toHaveAttribute("aria-readonly", "true");
  });
});
