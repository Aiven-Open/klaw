// ❗️ This implementation mirrors the Aiven core and is kept up to date with it.
// The Aiven core solution will be its own open source package soon.
// we're mirroring the implementation as it is, using only the components we actively use in coral.
// ❗️The Aiven core code base is the source of truth for this component.
import { Button } from "@aivenio/design-system";
import type { RenderResult } from "@testing-library/react";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import React from "react";
import type { DeepPartial, FieldValues } from "react-hook-form";
import {
  Form,
  SubmitErrorHandler,
  SubmitHandler,
  TextInput,
  useForm,
} from "src/app/components/Form";
import { z } from "zod";

type WrapperProps<T extends FieldValues> = {
  schema: any;
  defaultValues?: DeepPartial<T>;
  onSubmit: SubmitHandler<T>;
  onError: SubmitErrorHandler<T>;
};

const Wrapper = <T extends FieldValues>({
  schema,
  defaultValues,
  onSubmit,
  onError,
  children,
}: React.PropsWithChildren<WrapperProps<T>>): React.ReactElement => {
  const form = useForm<T>({ schema, defaultValues });
  return (
    <Form onSubmit={onSubmit} onError={onError} {...form}>
      {children}
    </Form>
  );
};

describe("Form", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();
  let results: RenderResult;
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    user = userEvent.setup();
  });

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  const renderForm = <T extends FieldValues>(
    children: React.ReactNode,
    { schema, defaultValues }: { schema: any; defaultValues?: DeepPartial<T> }
  ) => {
    return render(
      <Wrapper<T>
        schema={schema}
        defaultValues={defaultValues}
        onSubmit={onSubmit}
        onError={onError}
      >
        {children}
        <Button type="submit" title="Submit" />
      </Wrapper>
    );
  };

  const typeText = async (value: string) => {
    const el = screen.getByRole("textbox");
    await user.clear(el);
    await user.type(el, value);
  };

  const submit = async () => {
    await user.click(screen.getByRole("button", { name: "Submit" }));
  };

  const assertSubmitted = (data: any) => {
    expect(onSubmit).toHaveBeenCalledWith(data, expect.anything());
  };

  describe("<Form>", () => {
    const schema = z.object({ name: z.string().min(3, "error") });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <TextInput<Schema> name="name" labelText="TextInput" />,
        { schema }
      );
    });

    it("should call onSubmit() after submit if there are no validation errors", async () => {
      await user.type(screen.getByLabelText("TextInput"), "abc");
      await submit();
      await waitFor(() =>
        expect(onSubmit).toHaveBeenCalledWith(
          { name: "abc" },
          expect.anything()
        )
      );
    });

    it("should call onError() after submit if there are validation errors", async () => {
      await user.type(screen.getByLabelText("TextInput"), "a");
      await submit();
      await waitFor(() => expect(onError).toHaveBeenCalled());
      expect(onError.mock.calls[0][0]).toMatchObject({
        name: { message: "error" },
      });
    });
  });

  describe("<TextInput>", () => {
    const schema = z.object({ name: z.string().min(3, "error") });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <TextInput<Schema> name="name" labelText="TextInput" />,
        { schema }
      );
    });

    it("should render <TextInput>", () => {
      expect(results.container).toMatchSnapshot();
    });

    it("should render label", () => {
      expect(screen.queryByLabelText("TextInput")).toBeVisible();
    });

    it("should sync value to form state", async () => {
      await typeText("value{tab}");
      await submit();
      assertSubmitted({ name: "value" });
    });

    it("should render errors after blur event and hide them after valid input", async () => {
      await typeText("a{tab}");
      await waitFor(() => expect(screen.queryByText("error")).toBeVisible());

      await typeText("abc{tab}");
      await waitFor(() => expect(screen.queryByText("error")).toBeNull());
    });
  });
});
