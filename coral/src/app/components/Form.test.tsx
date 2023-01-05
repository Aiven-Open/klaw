import { Button, RadioButton as BaseRadioButton } from "@aivenio/aquarium";
import {
  cleanup,
  render,
  RenderResult,
  screen,
  waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import React from "react";
import type { DeepPartial, FieldValues } from "react-hook-form";
import {
  Form,
  NativeSelect,
  NumberInput,
  PasswordInput,
  RadioButton,
  RadioButtonGroup,
  SubmitErrorHandler,
  SubmitHandler,
  Textarea,
  TextInput,
  useForm,
} from "src/app/components/Form";
import { z, ZodSchema } from "zod";

type WrapperProps<T extends FieldValues> = {
  schema: ZodSchema;
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
    {
      schema,
      defaultValues,
    }: { schema: ZodSchema; defaultValues?: DeepPartial<T> }
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

  const assertSubmitted = (data: Record<string, string | number>) => {
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

  describe("<NumberInput>", () => {
    const schema = z.object({
      name: z.preprocess(
        (value) => parseInt(z.string().parse(value), 10),
        z.number().max(100)
      ),
    });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <NumberInput<Schema> name="name" labelText="NumberInput" />,
        { schema }
      );
    });

    it('renders <input type="number"', () => {
      const input = screen.getByLabelText("NumberInput");
      expect(input).toBeVisible();
      expect(input.getAttribute("type")).toBe("number");
      screen.getByRole("spinbutton", { name: "NumberInput" });
    });

    it("should sync value to form state", async () => {
      await user.type(
        screen.getByRole("spinbutton", { name: "NumberInput" }),
        "20"
      );
      await submit();
      assertSubmitted({ name: 20 });
    });

    it("should render errors after blur event and hide them after valid input", async () => {
      const errorMsg = "Number must be less than or equal to 100";
      await user.clear(screen.getByLabelText("NumberInput"));
      await user.type(screen.getByLabelText("NumberInput"), "200{tab}");
      await waitFor(() => expect(screen.queryByText(errorMsg)).toBeVisible());

      await user.clear(screen.getByLabelText("NumberInput"));
      await user.type(screen.getByLabelText("NumberInput"), "20{tab}");
      await waitFor(() => expect(screen.queryByText(errorMsg)).toBeNull());
    });
  });

  describe("<Textarea>", () => {
    const schema = z.object({ name: z.string().max(255) });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <Textarea<Schema> name="name" labelText="Textarea" />,
        { schema }
      );
    });

    it("should render label", () => {
      expect(screen.queryByLabelText("Textarea")).toBeVisible();
    });

    it("should sync value to form state", async () => {
      await typeText("value{tab}");
      await submit();
      assertSubmitted({ name: "value" });
    });

    it("should render errors after blur event and hide them after valid input", async () => {
      const errorMsg = "String must contain at most 255 character(s)";
      const tooLongValue = "a".repeat(256);
      await typeText(`${tooLongValue}{tab}`);
      await waitFor(() => expect(screen.queryByText(errorMsg)).toBeVisible());

      const okValue = "a".repeat(255);
      await typeText(`${okValue}{tab}`);
      await waitFor(() => expect(screen.queryByText(errorMsg)).toBeNull());
    });
  });

  describe("<NativeSelect>", () => {
    const schema = z.object({ name: z.enum(["helsinki", "berlin", "london"]) });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <NativeSelect<Schema> name="name" labelText="NativeSelect">
          <option value="helsinki">Helsinki</option>
          <option value="berlin">Berlin</option>
          <option value="london">London</option>
        </NativeSelect>,
        { schema }
      );
    });

    it("should render label", () => {
      expect(screen.getByLabelText("NativeSelect")).toBeVisible();
    });

    it("should render a combobox", () => {
      screen.getByRole("combobox", { name: "NativeSelect" });
    });

    it("should default to first available option", async () => {
      expect(screen.getByLabelText("NativeSelect")).toHaveDisplayValue(
        "Helsinki"
      );
      await submit();
      assertSubmitted({ name: "helsinki" });
    });

    it("should sync value to form state when choosing another option", async () => {
      await user.selectOptions(screen.getByLabelText("NativeSelect"), "berlin");
      expect(screen.getByLabelText("NativeSelect")).toHaveDisplayValue(
        "Berlin"
      );
      await submit();
      assertSubmitted({ name: "berlin" });
    });
  });

  describe("<PasswordInput>", () => {
    const schema = z.object({ password: z.string().min(3, "error") });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <PasswordInput<Schema> name="password" labelText="PasswordInput" />,
        { schema }
      );
    });

    it("should render <PasswordInput>", () => {
      expect(results.container).toMatchSnapshot();
    });

    it("should render label", () => {
      expect(screen.queryByLabelText("PasswordInput")).toBeVisible();
    });

    it("should sync value to form state", async () => {
      // For security reasons, there is no role password
      // -> https://github.com/w3c/aria/issues/166
      // the recommended way to query is using byLabel
      const passwordInput = screen.getByLabelText("PasswordInput");
      await user.clear(passwordInput);
      await user.type(passwordInput, "value{tab}");

      await submit();
      assertSubmitted({ password: "value" });
    });

    it("should render errors after blur event and hide them after valid input", async () => {
      // For security reasons, there is no role password
      // -> https://github.com/w3c/aria/issues/166
      // the recommended way to query is using byLabel
      const passwordInput = screen.getByLabelText("PasswordInput");
      await user.clear(passwordInput);
      await user.type(passwordInput, "a{tab}");
      await waitFor(() => expect(screen.queryByText("error")).toBeVisible());

      await user.clear(passwordInput);
      await user.type(passwordInput, "abc{tab}");
      await waitFor(() => expect(screen.queryByText("error")).toBeNull());
    });
  });

  describe("<RadioButton>", () => {
    const schema = z.object({
      city: z.string(),
    });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <RadioButton<Schema> name="city" value="Berlin">
          Berlin
        </RadioButton>,
        { schema }
      );
    });

    it("should render a RadioButton", () => {
      expect(screen.getByRole("radio")).toBeVisible();
    });

    it("should render correct label", () => {
      expect(screen.getByLabelText("Berlin")).toBeVisible();
    });

    it("should default to RadioButton being unchecked when no default values are provided", async () => {
      expect(screen.getByRole("radio")).not.toBeChecked();
    });

    it("should sync value to form state when clicking RadioButton", async () => {
      const berlinRadio = screen.getByRole("radio");
      expect(berlinRadio).not.toBeChecked();

      await user.click(berlinRadio);
      expect(berlinRadio).toBeChecked();

      await submit();
      assertSubmitted({ city: "Berlin" });
    });

    // @TODO accesibility testing once tab navigation is fixed for RadioButton
  });

  describe("<RadioButtonGroup>", () => {
    const schema = z.object({
      city: z.enum(["Berlin", "Helsinki"]),
    });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <RadioButtonGroup<Schema> name="city">
          <BaseRadioButton value="Berlin">Berlin</BaseRadioButton>
          <BaseRadioButton value="Helsinki">Helsinki</BaseRadioButton>
        </RadioButtonGroup>,
        { schema }
      );
    });

    it("should render two RadioButton", () => {
      expect(screen.getAllByRole("radio")).toHaveLength(2);
    });

    it("should render correct label", () => {
      expect(screen.getByLabelText("Berlin")).toBeVisible();
      expect(screen.getByLabelText("Helsinki")).toBeVisible();
    });

    it("should default to both RadioButton being unchecked when no default values are provided", async () => {
      expect(screen.getByLabelText("Berlin")).not.toBeChecked();
      expect(screen.getByLabelText("Helsinki")).not.toBeChecked();
    });

    it("should sync value to form state when clicking RadioButtons", async () => {
      const berlinRadio = screen.getByLabelText("Berlin");
      const helsinkiRadio = screen.getByLabelText("Helsinki");

      await user.click(berlinRadio);
      expect(berlinRadio).toBeChecked();
      expect(helsinkiRadio).not.toBeChecked();

      await submit();
      assertSubmitted({ city: "Berlin" });

      await user.click(helsinkiRadio);
      expect(berlinRadio).not.toBeChecked();
      expect(helsinkiRadio).toBeChecked();

      await submit();
      assertSubmitted({ city: "Helsinki" });
    });

    // @TODO accesibility testing once tab navigation is fixed for RadioButtonGroup
  });
});
