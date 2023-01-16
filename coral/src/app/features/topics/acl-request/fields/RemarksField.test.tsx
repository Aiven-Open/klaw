import { cleanup } from "@testing-library/react";
import RemarksField from "src/app/features/topics/acl-request/fields/RemarksField";
import { remarks } from "src/app/features/topics/acl-request/schemas/topic-acl-request-shared-fields";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const schema = z.object({
  remarks,
});

describe("Rema", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders a TextArea component", () => {
    const result = renderForm(<RemarksField />, { schema, onSubmit, onError });
    const textArea = result.getByRole("textbox");
    expect(textArea).toBeVisible();
    expect(textArea).toBeEnabled();
  });
});
