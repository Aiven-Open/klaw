import { NativeSelect, NativeSelectProps } from "@aivenio/aquarium";
import omit from "lodash/omit";
import { ChangeEvent, useState } from "react";

type ComplexNativeSelectProps<T> = NativeSelectProps & {
  options: Array<T>;
  identifierValue: keyof T;
  identifierName: keyof T;
  onBlur: (option: T | undefined) => void;
  placeholder: string;
  activeOption?: T;
};

function ComplexNativeSelect<T>(props: ComplexNativeSelectProps<T>) {
  const {
    options,
    onBlur,
    identifierValue,
    identifierName,
    placeholder,
    activeOption,
  } = props;

  const [activeValue, setActiveValue] = useState<T | undefined>(
    activeOption || undefined
  );

  function getValue(option: T): string {
    return String((option as T)[identifierValue]);
  }

  function getName(option: T): string {
    return String((option as T)[identifierName]);
  }

  // the placeholder behavior will be covered by DS NativeSelect
  // soon, this is a temp solution
  const placeholderValue = "055d87f2-8cd4-11ed-a1eb-0242ac120002";
  function setNewOption({ target: { value } }: ChangeEvent<HTMLSelectElement>) {
    if (value === placeholderValue) {
      onBlur(undefined);
    } else {
      const newOption = options.find((option) => getValue(option) === value);
      if (newOption) {
        setActiveValue(newOption);
        onBlur(newOption);
      }
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
      {...(!activeOption && { defaultValue: placeholderValue })}
    >
      {!activeOption && (
        <option key={placeholderValue} value={placeholderValue} disabled={true}>
          {placeholder}
        </option>
      )}
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
