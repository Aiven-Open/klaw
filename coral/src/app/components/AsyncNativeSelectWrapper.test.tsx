import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";
import { NativeSelect, Option, Typography } from "@aivenio/aquarium";
import { cleanup, render, screen, within } from "@testing-library/react";
import { AsyncNativeSelectWrapper } from "src/app/components/AsyncNativeSelectWrapper";
import { KlawApiError } from "src/services/api";
import { ComplexNativeSelect } from "src/app/components/ComplexNativeSelect";

const mockedUseToast = jest.fn();

jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

const testSelectLabel = "My test select";
const testNativeSelect = (
  <NativeSelect labelText={testSelectLabel}>
    <Option>Option one</Option>
    <Option>Option two</Option>
  </NativeSelect>
);

const testComplexNativeSelect = (
  <ComplexNativeSelect<{ id: string; name: string }>
    labelText={testSelectLabel}
    identifierValue={"id"}
    identifierName={"name"}
    options={[]}
    onBlur={() => null}
  />
);

const testProps = {
  entity: "test stuff",
  isLoading: false,
  isError: false,
  error: "",
};

describe("AsyncNativeSelectWrapper", () => {
  beforeAll(mockIntersectionObserver);

  describe("only accepts a <NativeSelect> or <ComplexNativeSelect> as child", () => {
    afterEach(cleanup);

    it("throws an error when child element is a html element", () => {
      expect(() =>
        render(
          <AsyncNativeSelectWrapper {...testProps}>
            <p>not correct</p>
          </AsyncNativeSelectWrapper>
        )
      ).toThrow(
        "Invalid child component. `AsyncNativeSelectWrapper` only accepts `NativeSelect` or `ComplexNativeSelect` as" +
          " a" +
          " child."
      );
    });

    it("throws an error when child element is a different DS component", () => {
      expect(() =>
        render(
          <AsyncNativeSelectWrapper {...testProps}>
            <Typography>not correct</Typography>
          </AsyncNativeSelectWrapper>
        )
      ).toThrow(
        "Invalid child component. `AsyncNativeSelectWrapper` only accepts `NativeSelect` or `ComplexNativeSelect` as" +
          " a" +
          " child."
      );
    });

    it("does not throw an error when child element is a DS NativeSelect", () => {
      expect(() =>
        render(
          <AsyncNativeSelectWrapper {...testProps}>
            {testNativeSelect}
          </AsyncNativeSelectWrapper>
        )
      ).not.toThrow();
    });

    it("does not throw an error when child element is a ComplexNativeSelect", () => {
      expect(() =>
        render(
          <AsyncNativeSelectWrapper {...testProps}>
            {testComplexNativeSelect}
          </AsyncNativeSelectWrapper>
        )
      ).not.toThrow();
    });
  });

  describe("shows a select element in loading state", () => {
    beforeAll(() => {
      render(
        <AsyncNativeSelectWrapper {...testProps} isLoading={true}>
          {testNativeSelect}
        </AsyncNativeSelectWrapper>
      );
    });

    afterAll(cleanup);

    it("shows a loading information and animation", () => {
      const loadingInfo = screen.getByTestId("async-select-loading-test-stuff");
      expect(loadingInfo).toBeVisible();
    });

    it("does not render the select element", () => {
      const select = screen.queryByRole("combobox", { name: testSelectLabel });
      expect(select).not.toBeInTheDocument();
    });
  });

  describe("handles errors when loading of content went wrong", () => {
    const testError: KlawApiError = {
      success: false,
      message: "Oh nooooo",
    };

    beforeAll(() => {
      render(
        <AsyncNativeSelectWrapper
          {...testProps}
          isError={true}
          error={testError}
        >
          {testNativeSelect}
        </AsyncNativeSelectWrapper>
      );
    });

    afterAll(cleanup);

    it("renders a disabled select element with information about missing data", () => {
      const select = screen.getByRole("combobox", { name: "No test stuff" });

      expect(select).toBeDisabled();
    });

    it("shows the label text given to the child NativeSelect", () => {
      const labelElement = screen.getByLabelText(
        testNativeSelect.props.labelText
      );
      const select = screen.getByRole("combobox");

      expect(select).toBe(labelElement);
    });

    it("marks the select element as invalid", () => {
      const select = screen.getByRole("combobox", { name: "No test stuff" });

      expect(select).toBeInvalid();
    });

    it("shows an error information beneath the select element", () => {
      const errorText = screen.getByText("Test stuff could not be loaded.");
      expect(errorText).toBeVisible();
    });

    it("notifies user about the error", () => {
      expect(mockedUseToast).toHaveBeenCalledWith({
        message: "Error loading test stuff: Oh nooooo",
        position: "bottom-left",
        variant: "default",
      });
    });
  });

  describe("shows the correct select element when content is loaded successfully", () => {
    beforeAll(() => {
      render(
        <AsyncNativeSelectWrapper {...testProps}>
          {testNativeSelect}
        </AsyncNativeSelectWrapper>
      );
    });

    afterAll(cleanup);

    it("renders the given select element", () => {
      const select = screen.getByRole("combobox", { name: testSelectLabel });
      expect(select).toBeEnabled();
    });

    it("renders the given select element", () => {
      const select = screen.getByRole("combobox", { name: testSelectLabel });
      expect(select).toBeEnabled();
      expect(select).toBeValid();
    });

    it("renders the correct children of the select element", () => {
      const select = screen.getByRole("combobox", { name: testSelectLabel });
      const options = within(select).getAllByRole("option");

      expect(options).toHaveLength(2);
      expect(options[0]).toHaveValue("Option one");
      expect(options[1]).toHaveValue("Option two");
    });
  });
});
