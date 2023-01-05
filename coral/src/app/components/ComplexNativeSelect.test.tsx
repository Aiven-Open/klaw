import { cleanup, render, within, screen } from "@testing-library/react";
import { ComplexNativeSelect } from "src/app/components/ComplexNativeSelect";
import userEvent from "@testing-library/user-event";

const testOptions = [
  {
    id: "1",
    name: "First option",
  },
  {
    id: "2",
    name: "Second option",
  },
  {
    id: "3",
    name: "Third option",
  },
];

const labelText = "Best option";
const placeholder = "Please select";

describe("ComplexNativeSelect.tsx", () => {
  describe("renders a select element with required properties", () => {
    const onBlurMock = jest.fn();
    const optionToStringMock = jest.fn((option) => option.name);
    const getValueMock = jest.fn((option) => option.id);

    const requiredProps = {
      options: testOptions,
      onBlur: onBlurMock,
      optionToString: optionToStringMock,
      getValue: getValueMock,
      placeholder: placeholder,
      labelText: labelText,
    };

    beforeAll(() => {
      render(<ComplexNativeSelect {...requiredProps} />);
    });

    afterAll(cleanup);

    it("shows a select element", () => {
      const select = screen.getByRole("combobox", { name: labelText });

      expect(select).toBeEnabled();
    });

    it("shows a given placeholder text as displayed value", () => {
      const select = screen.getByRole("combobox", { name: labelText });

      expect(select).toHaveDisplayValue(placeholder);
    });

    it("renders a list of given options including a disabled placeholder options", () => {
      const select = screen.getByRole("combobox", { name: labelText });
      const options = within(select).getAllByRole("option");

      expect(options).toHaveLength(testOptions.length + 1);
    });

    it("sets the option related to the placeholder as selected, but disabled", () => {
      const select = screen.getByRole("combobox", { name: labelText });
      const selectedOption = within(select).getByRole("option", {
        name: placeholder,
      });

      expect(selectedOption).toHaveAttribute("selected");
      expect(selectedOption).toBeDisabled();
    });

    testOptions.forEach((option) => {
      it(`shows the option "${option.name}"`, () => {
        const select = screen.getByRole("combobox", { name: labelText });
        const currOption = within(select).getByRole("option", {
          name: option.name,
        });

        expect(currOption).not.toHaveAttribute("selected");
        expect(currOption).toHaveValue(option.id);
        expect(currOption).toBeEnabled();
      });
    });
  });

  describe("renders a disabled select based on prop", () => {
    const onBlurMock = jest.fn();
    const optionToStringMock = jest.fn((option) => option.name);
    const getValueMock = jest.fn((option) => option.id);

    const requiredProps = {
      options: testOptions,
      onBlur: onBlurMock,
      optionToString: optionToStringMock,
      getValue: getValueMock,
      placeholder: placeholder,
      labelText: labelText,
    };

    beforeEach(() => {
      render(<ComplexNativeSelect {...requiredProps} disabled={true} />);
    });

    afterEach(cleanup);

    it("shows a disabled select element", () => {
      const select = screen.getByRole("combobox", { name: labelText });

      expect(select).toBeDisabled();
    });

    testOptions.forEach((option) => {
      it(`shows the option "${option.name}" as disabled`, () => {
        const select = screen.getByRole("combobox", { name: labelText });
        const currOption = within(select).getByRole("option", {
          name: option.name,
        });

        expect(currOption).not.toHaveAttribute("selected");
        expect(currOption).toHaveValue(option.id);
        expect(currOption).toBeDisabled();
      });
    });

    it("prevents user from interacting with the select", async () => {
      const select = screen.getByRole("combobox", { name: labelText });
      const option = within(select).getByRole("option", {
        name: testOptions[0].name,
      });

      await userEvent.selectOptions(select, option);

      expect(select).toHaveDisplayValue(placeholder);
      expect(option).not.toHaveAttribute("selected");
    });
  });

  describe("renders a required select based on prop", () => {
    const onBlurMock = jest.fn();
    const optionToStringMock = jest.fn((option) => option.name);
    const getValueMock = jest.fn((option) => option.id);

    const requiredProps = {
      options: testOptions,
      onBlur: onBlurMock,
      optionToString: optionToStringMock,
      getValue: getValueMock,
      placeholder: placeholder,
      labelText: labelText,
    };

    beforeEach(() => {
      render(<ComplexNativeSelect {...requiredProps} required={true} />);
    });

    afterEach(cleanup);

    it("shows a required select element", () => {
      const select = screen.getByRole("combobox", {
        name: new RegExp(`${labelText}`, "i"),
      });

      expect(select).toBeRequired();
    });
  });

  describe("renders an invalid select and shows an error message dependent on prop", () => {
    const onBlurMock = jest.fn();
    const optionToStringMock = jest.fn((option) => option.name);
    const getValueMock = jest.fn((option) => option.id);

    const errorMessage = "Oh no this is an error";

    const requiredProps = {
      options: testOptions,
      onBlur: onBlurMock,
      optionToString: optionToStringMock,
      getValue: getValueMock,
      placeholder: placeholder,
      labelText: labelText,
    };

    beforeEach(() => {
      render(<ComplexNativeSelect {...requiredProps} error={errorMessage} />);
    });

    afterEach(cleanup);

    it("shows an invalid select element", () => {
      const select = screen.getByRole("combobox", {
        name: new RegExp(`${labelText}`, "i"),
      });

      expect(select).toBeInvalid();
    });

    it("shows a given error message", () => {
      const errorMessage = screen.getByText("Oh no this is an error");

      expect(errorMessage).toBeVisible();
    });
  });

  describe("renders a given option as default value", () => {
    const onBlurMock = jest.fn();
    const optionToStringMock = jest.fn((option) => option.name);
    const getValueMock = jest.fn((option) => option.id);

    const selectedOption = testOptions[0];

    const requiredProps = {
      options: testOptions,
      onBlur: onBlurMock,
      optionToString: optionToStringMock,
      getValue: getValueMock,
      placeholder: placeholder,
      labelText: labelText,
    };

    beforeEach(() => {
      render(
        <ComplexNativeSelect
          {...requiredProps}
          defaultValue={selectedOption.id}
        />
      );
    });

    afterEach(cleanup);

    it("shows the name of the option as active value of the select", () => {
      const select = screen.getByRole("combobox", { name: labelText });

      expect(select).toHaveDisplayValue(selectedOption.name);
    });

    it("shows option related to the given option value as selected one", () => {
      const select = screen.getByRole("combobox", { name: labelText });
      const option = within(select).getByRole("option", {
        name: selectedOption.name,
      });

      expect(option).toHaveValue(selectedOption.id);
      expect(option).toHaveAttribute("selected");
    });

    it("does not render the disabled placeholder option", () => {
      const select = screen.getByRole("combobox", { name: labelText });
      const placeholderOption = within(select).queryByRole("option", {
        name: placeholder,
      });

      expect(placeholderOption).not.toBeInTheDocument();
    });
  });
});
