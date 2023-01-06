import { NativeSelect, NativeSelectProps } from "@aivenio/aquarium";
import { omit } from "lodash";
import { ChangeEvent, useState } from "react";

type ComplexNativeSelectProps<ComplexNativeSelectOptionType> =
  NativeSelectProps & {
    options: Array<ComplexNativeSelectOptionType>;
    identifierValue: keyof ComplexNativeSelectOptionType;
    identifierName: keyof ComplexNativeSelectOptionType;
    onBlur: (option: ComplexNativeSelectOptionType | undefined) => void;
    placeholder: string;
    activeOption?: ComplexNativeSelectOptionType;
  };

function ComplexNativeSelect<ComplexNativeSelectOptionType>(
  props: ComplexNativeSelectProps<ComplexNativeSelectOptionType>
) {
  const {
    options,
    onBlur,
    identifierValue,
    identifierName,
    placeholder,
    activeOption,
  } = props;

  const [activeValue, setActiveValue] = useState<
    ComplexNativeSelectOptionType | undefined
  >(activeOption || undefined);

  function getValue(option: ComplexNativeSelectOptionType): string {
    return String((option as ComplexNativeSelectOptionType)[identifierValue]);
  }

  function getName(option: ComplexNativeSelectOptionType): string {
    return String((option as ComplexNativeSelectOptionType)[identifierName]);
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
