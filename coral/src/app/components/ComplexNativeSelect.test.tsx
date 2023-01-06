import { cleanup, render, within, screen } from "@testing-library/react";
import { ComplexNativeSelect } from "src/app/components/ComplexNativeSelect";
import userEvent from "@testing-library/user-event";

type TestOption = {
  id: string;
  name: string;
};

const testOptions: Array<TestOption> = [
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
const labelTextRequired = new RegExp(`${labelText}`, "i");
const placeholder = "Please select";
const helperText = "There is an error";

describe("ComplexNativeSelect.tsx", () => {
  const onBlurMock = jest.fn();

  const requiredProps = {
    options: testOptions,
    identifierValue: "id" as keyof TestOption,
    identifierName: "name" as keyof TestOption,
    onBlur: onBlurMock,
    placeholder,
    labelText,
    helperText,
  };

  describe("renders an option select element with default props", () => {
    beforeAll(() => {
      render(<ComplexNativeSelect<TestOption> {...requiredProps} />);
    });

    afterAll(cleanup);

    it("shows a select element", () => {
      const select = screen.getByRole("combobox", { name: labelText });

      expect(select).toBeEnabled();
      expect(select).not.toBeRequired();
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
      it(`shows the option "${option.name}" with the right value associated to it`, () => {
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

  describe("renders an required select element dependent on props", () => {
    beforeAll(() => {
      render(
        <ComplexNativeSelect<TestOption> {...requiredProps} required={true} />
      );
    });

    afterAll(cleanup);

    it("shows a select element", () => {
      const select = screen.getByRole("combobox", { name: labelTextRequired });

      expect(select).toBeEnabled();
    });

    it("shows a required attribute to assistive technology", () => {
      const select = screen.getByRole("combobox", { name: labelTextRequired });

      expect(select).toBeRequired();
    });
  });

  describe("renders an invalid select element with error message dependent on props", () => {
    afterEach(cleanup);

    it("shows an invalid select element", () => {
      render(
        <ComplexNativeSelect<TestOption> {...requiredProps} valid={false} />
      );

      const select = screen.getByRole("combobox", { name: labelText });

      expect(select).toBeEnabled();
      expect(select).toBeInvalid();
    });

    it("shows a given error message to the user", () => {
      render(
        <ComplexNativeSelect<TestOption> {...requiredProps} valid={false} />
      );
      const errorMessage = screen.getByText(helperText);

      expect(errorMessage).toBeVisible();
    });

    it("does not show the error message as long as the select is valid", () => {
      render(
        <ComplexNativeSelect<TestOption> {...requiredProps} valid={true} />
      );
      const errorMessage = screen.queryByText(helperText);

      expect(errorMessage).not.toBeInTheDocument();
    });
  });

  describe("renders a given option as the active one when set by prop", () => {
    const selectedOption = testOptions[0];

    const requiredProps = {
      options: testOptions,
      identifierValue: "id" as keyof TestOption,
      identifierName: "name" as keyof TestOption,
      onBlur: onBlurMock,
      placeholder,
      labelText,
      helperText,
    };

    beforeEach(() => {
      render(
        <ComplexNativeSelect {...requiredProps} activeOption={selectedOption} />
      );
    });

    afterEach(cleanup);

    it("shows the name of the option as active value of the select", () => {
      const select = screen.getByRole("combobox", { name: labelText });

      expect(select).toHaveDisplayValue(selectedOption.name);
    });

    it("does not render the disabled placeholder option", () => {
      const select = screen.getByRole("combobox", { name: labelText });
      const placeholderOption = within(select).queryByRole("option", {
        name: placeholder,
      });

      expect(placeholderOption).not.toBeInTheDocument();
    });

    it("triggers the onBlur event with the set option when user leaves the select", async () => {
      const select = screen.getByRole("combobox", { name: labelText });
      select.focus();
      await userEvent.tab();

      expect(onBlurMock).toHaveBeenCalledWith(selectedOption);
    });
  });

  describe("handles user choosing an option an optional select", () => {
    beforeEach(() => {
      render(<ComplexNativeSelect<TestOption> {...requiredProps} />);
    });

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("enables user to select an option with mouse", async () => {
      const select = screen.getByRole("combobox", { name: labelText });
      const option = screen.getByRole("option", { name: testOptions[0].name });

      await userEvent.selectOptions(select, option);
      await userEvent.tab();

      expect(select).toHaveValue(testOptions[0].id);
      expect(onBlurMock).toHaveBeenCalledWith(testOptions[0]);
    });

    it("does not set an value and call onBlur if user didn't select an option", async () => {
      const select = screen.getByRole("combobox", { name: labelText });
      await userEvent.tab();

      expect(select).toHaveDisplayValue(placeholder);
      expect(onBlurMock).not.toHaveBeenCalled();
    });
  });
});
