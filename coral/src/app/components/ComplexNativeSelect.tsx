import { NativeSelect } from "@aivenio/aquarium";
import { ChangeEvent } from "react";

type ComplexNativeSelectProps<T> = {
  options: Array<T>;
  onBlur: (option: T | undefined) => void;
  optionToString: (opt: T) => string;
  getValue: (opt: T) => string;
  placeholder: string;
  labelText: string;
  disabled?: boolean;
  defaultValue?: string;
  required?: boolean;
  error?: string;
};

function ComplexNativeSelect<T>(props: ComplexNativeSelectProps<T>) {
  const {
    options,
    onBlur,
    disabled = false,
    required = false,
    defaultValue,
    labelText,
    optionToString,
    placeholder,
    getValue,
    error,
  } = props;

  // the placeholder behavior will be covered by DS NativeSelect
  // soon, this is a temp solution
  const placeholderValue = "055d87f2-8cd4-11ed-a1eb-0242ac120002";
  function setNewOption({ target: { value } }: ChangeEvent<HTMLSelectElement>) {
    if (value === placeholderValue) {
      onBlur(undefined);
    } else {
      const newOption = options.find((option) => getValue(option) === value);
      if (newOption) {
        onBlur(newOption);
      }
    }
  }

  return (
    <NativeSelect
      onBlur={setNewOption}
      disabled={disabled}
      labelText={labelText}
      required={required}
      defaultValue={defaultValue || placeholderValue}
      valid={error ? false : undefined}
      helperText={error}
    >
      {!defaultValue && (
        <option key={placeholderValue} value={placeholderValue} disabled={true}>
          {placeholder}
        </option>
      )}
      {options.map((option) => {
        const value = getValue(option);
        return (
          <option key={value} value={value}>
            {optionToString(option)}
          </option>
        );
      })}
    </NativeSelect>
  );
}

export { ComplexNativeSelect };
export type { ComplexNativeSelectProps };
