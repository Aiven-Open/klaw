import { NativeSelect, NativeSelectProps } from "@aivenio/aquarium";
import omit from "lodash/omit";
import { ChangeEvent, useState } from "react";

type ComplexNativeSelectProps<T> = NativeSelectProps & {
  options: Array<T>;
  identifierValue: keyof T;
  identifierName: keyof T;
  onBlur: (option: T | undefined) => void;
  activeOption?: T;
};

function ComplexNativeSelect<T>(props: ComplexNativeSelectProps<T>) {
  const { options, onBlur, identifierValue, identifierName, activeOption } =
    props;

  const [activeValue, setActiveValue] = useState<T | undefined>(
    activeOption || undefined
  );

  function getValue(option: T): string {
    return String((option as T)[identifierValue]);
  }

  function getName(option: T): string {
    return String((option as T)[identifierName]);
  }

  function setNewOption({ target: { value } }: ChangeEvent<HTMLSelectElement>) {
    const newOption = options.find((option) => getValue(option) === value);
    if (newOption) {
      setActiveValue(newOption);
      onBlur(newOption);
    } else {
      // trigger onBlur with undefined so a required field
      // is marked invalid in case no value (or placeholder)
      // is choosen.
      onBlur(undefined);
    }
  }

  const nativeSelectProps = omit(
    props,
    "options",
    "identifierValue",
    "identifierName",
    "activeOption"
  );

  return (
    <NativeSelect
      {...nativeSelectProps}
      value={activeValue && getValue(activeValue)}
      onBlur={setNewOption}
      onChange={setNewOption}
    >
      {options.map((option) => {
        const value = getValue(option);
        return (
          <option key={value} value={value}>
            {getName(option)}
          </option>
        );
      })}
    </NativeSelect>
  );
}

export { ComplexNativeSelect };
export type { ComplexNativeSelectProps };
