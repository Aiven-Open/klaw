import { RadioButton as BaseRadioButton } from "@aivenio/aquarium";
import { cleanup, RenderResult, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  ComplexNativeSelect,
  FileInput,
  MultiInput,
  MultiSelect,
  NativeSelect,
  NumberInput,
  PasswordInput,
  RadioButton,
  RadioButtonGroup,
  Textarea,
  TextInput,
} from "src/app/components/Form";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

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

  const typeText = async (value: string) => {
    const el = screen.getByRole("textbox");
    await user.clear(el);
    await user.type(el, value);
  };

  const submit = async () => {
    await user.click(screen.getByRole("button", { name: "Submit" }));
  };

  const assertSubmitted = (
    data: Record<string, string | number | string[]>
  ) => {
    expect(onSubmit).toHaveBeenCalledWith(data, expect.anything());
  };

  describe("<Form>", () => {
    const schema = z.object({
      formFieldsCustomName: z.string().min(3, "error"),
    });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <TextInput<Schema> name="formFieldsCustomName" labelText="TextInput" />,
        { schema, onSubmit, onError }
      );
    });

    it("should call onSubmit() after submit if there are no validation errors", async () => {
      await user.type(screen.getByLabelText("TextInput"), "abc");
      await submit();
      await waitFor(() =>
        expect(onSubmit).toHaveBeenCalledWith(
          { formFieldsCustomName: "abc" },
          expect.anything()
        )
      );
    });

    it("should call onError() after submit if there are validation errors", async () => {
      await user.type(screen.getByLabelText("TextInput"), "a");
      await submit();
      await waitFor(() => expect(onError).toHaveBeenCalled());
      expect(onError.mock.calls[0][0]).toMatchObject({
        formFieldsCustomName: { message: "error" },
      });
    });

    it("should not call onSubmit() after submit if there are validation errors", async () => {
      await user.type(screen.getByLabelText("TextInput"), "a");
      await submit();
      await waitFor(() => expect(onSubmit).not.toHaveBeenCalled());
    });

    it("should render enabled Submit button even if there are validation errors", async () => {
      await user.type(screen.getByLabelText("TextInput"), "a");

      const submitButton = screen.getByRole("button", { name: "Submit" });

      expect(submitButton).toBeEnabled();
    });
  });

  describe("<TextInput>", () => {
    const schema = z.object({
      formFieldsCustomName: z.string().min(3, "error"),
    });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <TextInput<Schema> name="formFieldsCustomName" labelText="TextInput" />,
        { schema, onSubmit, onError }
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
      assertSubmitted({ formFieldsCustomName: "value" });
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
      number: z.preprocess(
        (value) => parseInt(z.string().parse(value), 10),
        z.number().max(100)
      ),
    });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <NumberInput<Schema> name="number" labelText="NumberInput" />,
        { schema, onSubmit, onError }
      );
    });

    it('renders <input type="number"', () => {
      const input = screen.getByRole("spinbutton", { name: "NumberInput" });
      expect(input).toBeEnabled();
    });

    it("should sync value to form state", async () => {
      await user.type(
        screen.getByRole("spinbutton", { name: "NumberInput" }),
        "20"
      );
      await submit();
      assertSubmitted({ number: 20 });
    });

    it("should render errors after blur event and hide them after valid input", async () => {
      const errorMsgEmpty = "Expected number, received nan";
      const errorMsgValidation = "Number must be less than or equal to 100";

      const input = screen.getByRole("spinbutton", { name: "NumberInput" });

      expect(input).toHaveValue(null);
      await user.click(input);
      await user.tab();

      expect(await screen.findByText(errorMsgEmpty)).toBeVisible();

      await user.type(input, "200{tab}");
      expect(await screen.findByText(errorMsgValidation)).toBeVisible();

      await user.clear(input);

      await user.type(input, "20{tab}");

      expect(screen.queryByText(errorMsgEmpty)).not.toBeInTheDocument();
      expect(screen.queryByText(errorMsgValidation)).not.toBeInTheDocument();
    });
  });

  describe("<Textarea>", () => {
    const schema = z.object({ formFieldsCustomName: z.string().max(255) });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <Textarea<Schema> name="formFieldsCustomName" labelText="Textarea" />,
        { schema, onSubmit, onError }
      );
    });

    it("should render label", () => {
      expect(screen.queryByLabelText("Textarea")).toBeVisible();
    });

    it("should sync value to form state", async () => {
      await typeText("value{tab}");
      await submit();
      assertSubmitted({ formFieldsCustomName: "value" });
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
    describe("handles a select without placeholder", () => {
      const schema = z.object({
        formFieldsCustomName: z.enum(["helsinki", "berlin", "london"]),
      });
      type Schema = z.infer<typeof schema>;

      beforeEach(() => {
        results = renderForm(
          <NativeSelect<Schema>
            name="formFieldsCustomName"
            labelText="NativeSelect"
          >
            <option value="helsinki">Helsinki</option>
            <option value="berlin">Berlin</option>
            <option value="london">London</option>
          </NativeSelect>,
          { schema, onSubmit, onError }
        );
      });

      it("should render label", () => {
        expect(screen.getByLabelText("NativeSelect")).toBeVisible();
      });

      it("should render a combobox", () => {
        screen.getByRole("combobox", { name: "NativeSelect" });
      });

      it("defaults to the first option as chosen one", async () => {
        const select = screen.getByRole("combobox", { name: "NativeSelect" });

        expect(select).toHaveDisplayValue("Helsinki");
        await userEvent.click(select);
        await userEvent.keyboard("{Escape}");
        await userEvent.tab();

        await submit();
        assertSubmitted({ formFieldsCustomName: "helsinki" });
      });

      it("should sync value to form state when choosing option", async () => {
        await user.selectOptions(
          screen.getByLabelText("NativeSelect"),
          "berlin"
        );
        expect(screen.getByLabelText("NativeSelect")).toHaveDisplayValue(
          "Berlin"
        );
        await submit();
        assertSubmitted({ formFieldsCustomName: "berlin" });
      });
    });

    describe("handles a select with a placeholder where a certain value is required", () => {
      const schema = z.object({
        formFieldSelect: z.string().min(1, "Form field is required"),
      });
      type Schema = z.infer<typeof schema>;

      beforeEach(() => {
        renderForm(
          <NativeSelect<Schema>
            name="formFieldSelect"
            labelText="NativeSelect"
            placeholder={"placeholder text"}
          >
            <option value="spring">Spring</option>
            <option value="summer">Summer</option>
            <option value="autumn">Autumn</option>
            <option value="winter">Winter</option>
          </NativeSelect>,
          { schema, onSubmit, onError }
        );
      });

      it("shows a placeholder value", async () => {
        const select = screen.getByRole("combobox", { name: "NativeSelect" });

        expect(select).toHaveDisplayValue("placeholder text");
      });

      it("shows an error message if user does not select an option", async () => {
        const select = screen.getByRole("combobox", { name: "NativeSelect" });

        await userEvent.click(select);
        await userEvent.keyboard("{Escape}");
        await userEvent.tab();

        const errorMessage = await screen.findByText("Form field is required");
        expect(errorMessage).toBeVisible();
      });
    });

    describe("handles an optional select with a placeholder", () => {
      const schema = z.object({
        formFieldSelect: z.string().optional(),
      });
      type Schema = z.infer<typeof schema>;

      beforeEach(() => {
        renderForm(
          <NativeSelect<Schema>
            name="formFieldSelect"
            labelText="NativeSelect"
            placeholder={"placeholder text"}
          >
            <option value="spring">Spring</option>
            <option value="summer">Summer</option>
            <option value="autumn">Autumn</option>
            <option value="winter">Winter</option>
          </NativeSelect>,
          { schema, onSubmit, onError }
        );
      });

      it("should sync an empty value to form state when choosing option", async () => {
        const select = screen.getByRole("combobox", { name: "NativeSelect" });

        await userEvent.click(select);
        await userEvent.keyboard("{Escape}");
        await userEvent.tab();

        await submit();
        assertSubmitted({ formFieldSelect: "" });
      });
    });
  });

  describe("<PasswordInput>", () => {
    const schema = z.object({ password: z.string().min(3, "error") });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <PasswordInput<Schema> name="password" labelText="PasswordInput" />,
        { schema, onSubmit, onError }
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
        { schema, onSubmit, onError }
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
        { schema, onSubmit, onError }
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

  describe("<ComplexNativeSelect>", () => {
    const schema = z.object({
      formFieldsCustomName: z.object(
        {
          name: z.string(),
          id: z.string(),
          age: z.number(),
        },
        { required_error: "This is required" }
      ),
    });

    type Schema = z.infer<typeof schema>;

    type TestOption = {
      id: string;
      name: string;
      age: number;
    };

    const testOptions: Array<TestOption> = [
      { id: "1", name: "one", age: 1 },
      { id: "2", name: "two", age: 2 },
      { id: "3", name: "three", age: 3 },
    ];

    beforeEach(() => {
      results = renderForm(
        <ComplexNativeSelect<Schema, TestOption>
          name="formFieldsCustomName"
          options={testOptions}
          labelText="ComplexNativeSelect"
          identifierValue={"id"}
          identifierName={"name"}
          placeholder={"Please select"}
        />,
        { schema, onSubmit, onError }
      );
    });

    it("renders a select element with a given placeholder", () => {
      const select = screen.getByRole("combobox", {
        name: "ComplexNativeSelect",
      });

      expect(select).toBeEnabled();
      expect(select).toHaveDisplayValue("Please select");
    });

    it("syncs value to form state when user chooses an option", async () => {
      const select = screen.getByRole("combobox", {
        name: "ComplexNativeSelect",
      });

      const selectedOption = testOptions[1];
      const option = screen.getByRole("option", {
        name: selectedOption.name,
      });

      await user.selectOptions(select, option);
      await user.tab();

      expect(select).toHaveDisplayValue(selectedOption.name);
      await submit();

      expect(onSubmit).toHaveBeenCalledWith(
        { formFieldsCustomName: selectedOption },
        expect.anything()
      );
    });

    it("does not syncs value to form state when user did not choose an option", async () => {
      const select = screen.getByRole("combobox", {
        name: "ComplexNativeSelect",
      });
      select.focus();
      await user.tab();

      expect(select).toHaveDisplayValue("Please select");
      await submit();

      expect(onSubmit).not.toHaveBeenCalled();
    });

    it("shows an error when user did not choose an option and wants to submit", async () => {
      const select = screen.getByRole("combobox", {
        name: "ComplexNativeSelect",
      });
      expect(select).toBeValid();

      select.focus();
      await user.tab();

      await submit();

      expect(select).toBeInvalid();
      expect(screen.getByText("This is required")).toBeVisible();
    });

    it("shows an error when user did not choose an option and leaves the field", async () => {
      const select = screen.getByRole("combobox", {
        name: "ComplexNativeSelect",
      });
      expect(select).toBeValid();

      select.focus();
      await user.keyboard("{ArrowDown}");
      await user.keyboard("{ESC}");
      await user.tab();

      await waitFor(() => expect(select).toBeInvalid());
      expect(screen.getByText("This is required")).toBeVisible();
    });
  });

  describe("<MultiInput>", () => {
    const schema = z.object({
      cities: z.string().array().nonempty(),
    });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <MultiInput<Schema> name="cities" labelText="Cities" />,
        { schema, onSubmit, onError }
      );
    });

    it("should render a MultiInput", () => {
      expect(screen.getByRole("textbox")).toBeVisible();
    });

    it("should render correct label", () => {
      expect(screen.getByLabelText("Cities")).toBeVisible();
    });

    it("should sync value to form state when typing into MultiInput", async () => {
      const citiesInput = screen.getByRole<HTMLInputElement>("textbox");

      await user.type(citiesInput, "Berlin");
      expect(citiesInput.value).toBe("Berlin");
      await userEvent.keyboard("{Enter}");

      await user.type(citiesInput, "Helsinki");
      expect(citiesInput.value).toBe("Helsinki");
      await user.keyboard("{Enter}");

      const berlinPill = screen.getByText("Berlin");
      const helsinkiPill = screen.getByText("Helsinki");
      expect(berlinPill).toBeVisible();
      expect(helsinkiPill).toBeVisible();
      expect(screen.getByRole("button", { name: "Submit" })).toBeVisible();
      expect(screen.getByRole("button", { name: "Submit" })).toBeEnabled();

      await submit();
      assertSubmitted({ cities: ["Berlin", "Helsinki"] });
    });

    it("shows an error if user does not fill out required field and wants to submit", async () => {
      const errorMsgEmpty = "Required";
      const citiesInput = screen.getByRole<HTMLInputElement>("textbox");

      expect(screen.queryByText(errorMsgEmpty)).not.toBeInTheDocument();
      expect(citiesInput).toBeValid();

      await user.click(citiesInput);
      await user.tab();

      await submit();

      await waitFor(() => expect(citiesInput).toBeInvalid());
      expect(screen.getByText(errorMsgEmpty)).toBeVisible();
      expect(onSubmit).not.toHaveBeenCalled();
    });
  });

  describe("<MultiSelect>", () => {
    const schema = z.object({
      cities: z.string().array(),
    });
    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <MultiSelect<Schema, string>
          name="cities"
          labelText="Cities"
          options={["Helsinki", "Paris", "Berlin"]}
          noResults="No cities"
          createOption={(newOption) => {
            if (newOption === undefined) {
              return;
            }
            return newOption;
          }}
        />,
        { schema, onSubmit, onError }
      );
    });

    it("should render a MultiSelect", () => {
      expect(screen.getByRole("combobox", { name: "Cities" })).toBeVisible();
    });

    it("should render correct label", () => {
      expect(screen.getByLabelText("Cities")).toBeVisible();
    });

    it("should sync value to form state when selecting an option in MultiSelect", async () => {
      const citiesInput = screen.getByRole("combobox", { name: "Cities" });

      await userEvent.click(citiesInput);
      await userEvent.click(
        screen.getByRole("option", {
          name: "Berlin",
        })
      );

      await user.tab();

      expect(screen.getByRole("button", { name: "Berlin" })).toBeVisible();

      expect(screen.getByRole("button", { name: "Submit" })).toBeVisible();
      expect(screen.getByRole("button", { name: "Submit" })).toBeEnabled();

      await submit();
      assertSubmitted({ cities: ["Berlin"] });
    });

    it("should sync value to form state when entering a new option in MultiSelect", async () => {
      const citiesInput = screen.getByRole("combobox", { name: "Cities" });

      await user.type(citiesInput, "NYC");
      await user.tab();

      expect(screen.getByRole("button", { name: "NYC" })).toBeVisible();

      expect(screen.getByRole("button", { name: "Submit" })).toBeVisible();
      expect(screen.getByRole("button", { name: "Submit" })).toBeEnabled();

      await submit();
      assertSubmitted({ cities: ["NYC"] });
    });

    it("shows an error if user does not fill out required field and wants to submit", async () => {
      const errorMsgEmpty = "Required";
      const citiesInput = screen.getByRole("combobox", { name: "Cities" });

      expect(screen.queryByText(errorMsgEmpty)).not.toBeInTheDocument();
      expect(citiesInput).toBeValid();

      await user.click(citiesInput);
      await user.tab();

      await submit();

      await waitFor(() => expect(citiesInput).toBeInvalid());
      expect(screen.getByText(errorMsgEmpty)).toBeVisible();
      expect(onSubmit).not.toHaveBeenCalled();
    });
  });

  describe("<FileInput>", () => {
    const schema = z.object(
      {
        image: z.instanceof(File),
      },
      {
        required_error: "A file is required",
        invalid_type_error: "This is not a valid file",
      }
    );

    type Schema = z.infer<typeof schema>;

    beforeEach(() => {
      results = renderForm(
        <FileInput<Schema>
          name={"image"}
          labelText={"Please upload a file"}
          buttonText={"Upload"}
          noFileText={"No file chose"}
        />,
        { schema, onSubmit, onError }
      );
    });

    it("renders an optional file upload input", () => {
      // input type=file does not have a role to look for
      const fileInput = screen.getByLabelText<HTMLInputElement>(
        "Please upload a file"
      );

      expect(fileInput).not.toBeRequired();
      expect(fileInput).toBeEnabled();
      expect(fileInput.tagName).toBe("INPUT");
    });

    it("renders the file input as required dependent on prop", () => {
      // component is not set to required for all the other
      // test cases because we can't "upload" a valid file with
      // content in test, so this block has it's own cleanup/render
      cleanup();
      renderForm(
        <FileInput<Schema>
          name={"image"}
          labelText={"Please upload a file"}
          buttonText={"Upload"}
          noFileText={"No file chose"}
          required={true}
        />,
        { schema, onSubmit, onError }
      );
      // input type=file does not have a role to look for
      const fileInput = screen.getByLabelText<HTMLInputElement>(
        "Please upload a file"
      );

      expect(fileInput).toBeRequired();
      expect(fileInput).toBeEnabled();
      expect(fileInput.tagName).toBe("INPUT");
    });

    it("syncs value to form state when user uploads a file", async () => {
      const fileName = "my-awesome-dog.jpeg";
      const testFile: File = new File(["I am a dog picture"], fileName, {
        type: "image/jpeg",
      });

      const fileInput = screen.getByLabelText<HTMLInputElement>(
        "Please upload a file"
      );
      await user.upload(fileInput, testFile);

      expect(fileInput.files?.[0]).toBe(testFile);
      await submit();

      expect(onSubmit).toHaveBeenCalledWith(
        { image: testFile },
        expect.anything()
      );
    });

    it("shows an error for an invalid file", async () => {
      const fileName = "my-awesome-dog-not-file";
      const invalidInput = { name: fileName };

      const fileInput = screen.getByLabelText<HTMLInputElement>(
        "Please upload a file"
      );
      // it's invalid input on purpose
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      await user.upload(fileInput, invalidInput);

      await submit();
      const errorMessage = screen.getByText("Input not instance of File");
      expect(onSubmit).not.toHaveBeenCalled();

      expect(errorMessage).toBeVisible();
    });
  });
});
