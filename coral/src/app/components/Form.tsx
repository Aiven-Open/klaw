import {
  Input as BaseInput,
  InputProps as BaseInputProps,
  MultiInput as BaseMultiInput,
  MultiInputProps as BaseMultiInputProps,
  MultiSelect as BaseMultiSelect,
  MultiSelectProps as BaseMultiSelectProps,
  NativeSelect as BaseNativeSelect,
  NativeSelectProps as BaseNativeSelectProps,
  RadioButton as BaseRadioButton,
  RadioButtonGroup as BaseRadioButtonGroup,
  RadioButtonGroupProps as BaseRadioButtonGroupProps,
  RadioButtonProps as BaseRadioButtonProps,
  Textarea as BaseTextarea,
  TextareaProps as BaseTextareaProps,
  Option,
  OptionType,
  PrimaryButton,
} from "@aivenio/aquarium";
import { zodResolver } from "@hookform/resolvers/zod";
import get from "lodash/get";
import omit from "lodash/omit";
import React, { ChangeEvent, memo } from "react";
import type { FormState } from "react-hook-form";
import {
  FieldError,
  FieldPath,
  FieldValues,
  FormProvider,
  SubmitErrorHandler,
  SubmitHandler,
  UseFormReturn,
  Controller as _Controller,
  UseFormProps as _UseFormProps,
  useForm as _useForm,
  useFormContext,
  useWatch,
} from "react-hook-form";
import {
  ComplexNativeSelect as BaseComplexNativeSelect,
  ComplexNativeSelectProps as BaseComplexNativeSelectProps,
} from "src/app/components/ComplexNativeSelect";
import {
  FileInput as BaseFileInput,
  FileInputProps as BaseFileInputProps,
} from "src/app/components/FileInput";
import { ZodSchema } from "zod";

type FormInputProps<T extends FieldValues = FieldValues> = {
  name: FieldPath<T>;
};

type FormRegisterProps<T extends FieldValues = FieldValues> = {
  formContext: UseFormReturn<T>;
};

// eslint-disable-next-line import/exports-last
export type { SubmitHandler, SubmitErrorHandler, FieldError };
// eslint-disable-next-line import/exports-last,import/group-exports
export { useWatch, Option };

type UseFormProps<T extends FieldValues = FieldValues> = Omit<
  _UseFormProps<T>,
  "resolver"
> & {
  schema?: ZodSchema;
};

// eslint-disable-next-line import/exports-last,import/group-exports
export const useForm = <T extends FieldValues = FieldValues>({
  defaultValues,
  schema,
  ...props
}: UseFormProps<T>): UseFormReturn<T> => {
  return _useForm<T>({
    ...props,
    mode: "onTouched",
    defaultValues,
    resolver: schema && zodResolver(schema),
  });
};

type FormProps<T extends FieldValues = FieldValues> = UseFormReturn<T> & {
  onSubmit: SubmitHandler<T>;
  onError?: SubmitErrorHandler<T>;
  ariaLabel?: string;
};

// eslint-disable-next-line import/exports-last,import/group-exports
export const Form = <T extends FieldValues = FieldValues>({
  onSubmit,
  onError,
  ariaLabel,
  children,
  ...form
}: React.PropsWithChildren<FormProps<T>>): React.ReactElement<FormProps<T>> => {
  return (
    <FormProvider {...form}>
      <form
        aria-label={ariaLabel}
        onSubmit={form.handleSubmit(onSubmit, onError)}
      >
        {children}
      </form>
    </FormProvider>
  );
};

/** <PasswordInput>
 * This not part of Aiven core implementation but an input
 * custom for Klaw use cases. It's exactly the same as <TextInput>
 * with the only difference being the type (password) to have
 * a more secure way for users to enter password (input obscured)
 */
function _PasswordInput<T extends FieldValues>({
  name,
  formContext: form,
  ...props
}: BaseInputProps & FormInputProps<T> & FormRegisterProps<T>) {
  const { errors } = form.formState;
  const error = get(errors, name)?.message as string;
  return (
    <BaseInput
      {...props}
      type="password"
      {...form.register(name)}
      valid={error ? false : undefined}
      error={error}
    />
  );
}

const PasswordInputMemo = memo(_PasswordInput) as typeof _PasswordInput;

// eslint-disable-next-line import/exports-last,import/group-exports
export const PasswordInput = <T extends FieldValues>(
  props: FormInputProps<T> & BaseInputProps
): React.ReactElement<FormInputProps<T> & BaseInputProps> => {
  const ctx = useFormContext<T>();
  return <PasswordInputMemo formContext={ctx} {...props} />;
};

PasswordInput.Skeleton = BaseInput.Skeleton;

//
// <TextInput>
//
function _TextInput<T extends FieldValues>({
  name,
  formContext: form,
  ...props
}: BaseInputProps & FormInputProps<T> & FormRegisterProps<T>) {
  const { errors } = form.formState;
  const error = get(errors, name)?.message as string;
  return (
    <BaseInput
      {...props}
      type="text"
      {...form.register(name)}
      valid={error ? false : undefined}
      error={error}
    />
  );
}

const TextInputMemo = memo(_TextInput) as typeof _TextInput;

// eslint-disable-next-line import/exports-last,import/group-exports
export const TextInput = <T extends FieldValues>(
  props: FormInputProps<T> & BaseInputProps
): React.ReactElement<FormInputProps<T> & BaseInputProps> => {
  const ctx = useFormContext<T>();
  return <TextInputMemo formContext={ctx} {...props} />;
};

TextInput.Skeleton = BaseInput.Skeleton;

//
// <NumberInput>
//
function _NumberInput<T extends FieldValues>({
  name,
  formContext: form,
  ...props
}: BaseInputProps & FormInputProps<T> & FormRegisterProps<T>) {
  const error = parseFieldErrorMessage(form.formState, name);
  return (
    <BaseInput
      {...props}
      type="number"
      {...form.register(name)}
      valid={error === undefined}
      error={error}
    />
  );
}

const NumberInputMemo = memo(
  _NumberInput, // eslint-disable-next-line @typescript-eslint/no-unused-vars
  (_prev: FormRegisterProps, _next: FormRegisterProps) => {
    return false;
  }
) as typeof _NumberInput;

// eslint-disable-next-line import/exports-last,import/group-exports
export const NumberInput = <T extends FieldValues>(
  props: FormInputProps<T> & BaseInputProps
): React.ReactElement<FormInputProps<T> & BaseInputProps> => {
  const ctx = useFormContext<T>();
  return <NumberInputMemo formContext={ctx} {...props} />;
};

NumberInput.Skeleton = BaseInput.Skeleton;

//
// <Textarea>
//
function _Textarea<T extends FieldValues>({
  name,
  formContext: form,
  ...props
}: BaseTextareaProps & FormInputProps<T> & FormRegisterProps<T>) {
  const error = parseFieldErrorMessage(form.formState, name);
  return (
    <BaseTextarea
      {...props}
      {...form.register(name)}
      valid={error === undefined}
      error={error}
    />
  );
}
const TextareaMemo = memo(_Textarea, () => false) as typeof _Textarea;

// eslint-disable-next-line import/exports-last,import/group-exports
export const Textarea = <T extends FieldValues>(
  props: FormInputProps<T> & BaseTextareaProps
): React.ReactElement<FormInputProps<T> & BaseInputProps> => {
  const ctx = useFormContext<T>();
  return <TextareaMemo formContext={ctx} {...props} />;
};

Textarea.Skeleton = BaseTextarea.Skeleton;

//
// <MultiInput>
//
function _MultiInput<T extends FieldValues>({
  name,
  helperText,
  formContext: form,
  ...props
}: BaseMultiInputProps<T> & FormInputProps<T> & FormRegisterProps<T>) {
  // Field level error (eg "Cannot be empty")
  const error = parseFieldErrorMessage(form.formState, name);
  // Single items level error (MultiInput returns an array of errors when single items fail validation)
  const itemsErrors = parseFieldErrorsArray(form.formState, name);
  const itemErrorsAccumulatedMessages = (itemsErrors || [])
    .reduce(
      (accumulatedErrorMessages: string[], currentItemError): string[] => {
        if (
          currentItemError.message === undefined ||
          accumulatedErrorMessages.includes(currentItemError.message)
        ) {
          return accumulatedErrorMessages;
        }
        return [...accumulatedErrorMessages, currentItemError.message];
      },
      []
    )
    .join(", ");
  const isValid = error === undefined && itemErrorsAccumulatedMessages === "";

  return (
    <_Controller
      name={name}
      control={form.control}
      render={({ field: { value, name } }) => {
        return (
          <BaseMultiInput
            {...props}
            name={name}
            value={value}
            onChange={(value) => {
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              form.setValue(name, value as any, {
                shouldValidate: true,
                shouldDirty: true,
              });
            }}
            valid={isValid}
            isItemValid={(_, index) => {
              if (itemsErrors === undefined) {
                return true;
              }

              const isCurrentItemValid = itemsErrors[index] === undefined;

              return isCurrentItemValid;
            }}
            error={error}
            helperText={
              itemsErrors !== undefined
                ? itemErrorsAccumulatedMessages
                : helperText
            }
          />
        );
      }}
    />
  );
}

const MultiInputMemo = memo(_MultiInput) as typeof _MultiInput;

// eslint-disable-next-line import/exports-last,import/group-exports
export const MultiInput = <T extends FieldValues>(
  props: FormInputProps<T> & BaseMultiInputProps<T>
): React.ReactElement<FormInputProps<T> & BaseMultiInputProps<T>> => {
  const ctx = useFormContext<T>();
  return <MultiInputMemo formContext={ctx} {...props} />;
};

MultiInput.Skeleton = BaseMultiInput.Skeleton;

//
// <MultiSelect>
//
function _MultiSelect<T extends FieldValues, FieldValue>({
  name,
  formContext: form,
  ...props
}: BaseMultiSelectProps<FieldValue> &
  FormInputProps<T> &
  FormRegisterProps<T>) {
  return (
    <_Controller
      name={name}
      control={form.control}
      render={({ field: { name } }) => {
        const { isSubmitting } = form.formState;
        const error = parseFieldErrorMessage(form.formState, name);
        return (
          <BaseMultiSelect
            {...props}
            name={name}
            disabled={props.disabled || isSubmitting}
            onChange={(values) => {
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              form.setValue(name, values as any, {
                shouldValidate: true,
                shouldDirty: true,
              });
            }}
            valid={error === undefined}
            helperText={error}
          />
        );
      }}
    />
  );
}

const MultiSelectMemo = memo(_MultiSelect) as typeof _MultiSelect;

// eslint-disable-next-line import/exports-last,import/group-exports
export const MultiSelect = <T extends FieldValues, FieldValue>(
  props: FormInputProps<T> & BaseMultiSelectProps<FieldValue>
): React.ReactElement<FormInputProps<T> & BaseMultiSelectProps<FieldValue>> => {
  const ctx = useFormContext<T>();
  return <MultiSelectMemo formContext={ctx} {...props} />;
};

MultiInput.Skeleton = BaseMultiInput.Skeleton;

//
// <NativeSelect>
//
function _NativeSelect<T extends FieldValues>({
  name,
  formContext: form,
  disabled,
  ...props
}: BaseNativeSelectProps & FormInputProps<T> & FormRegisterProps<T>) {
  return (
    <_Controller
      name={name}
      control={form.control}
      render={({ field: { name }, fieldState: { error } }) => {
        const { isSubmitting } = form.formState;
        const value = form.getValues(name);

        // makes sure NativeSelect in Form works without logging warning
        // about use of value and defaultValue when:
        // - it has placeholder AND defaultValue (which is not a usual case),
        // - value is updated not through user input
        const baseNativeProps = !value
          ? props
          : { ...omit(props, "placeholder"), value: value };
        return (
          <BaseNativeSelect
            {...baseNativeProps}
            name={name}
            disabled={disabled || isSubmitting}
            aria-readonly={props.readOnly}
            onBlur={(option) => {
              // Prevent from setting empty string value in form when blurring out of the component
              // Without selecting a value other than the placeholder
              // ie: clicking outside, tabbing out
              // This allows to:
              // - not set an empty value which may throw off the rest of validation in the form
              // - keep the field validation in case of required field
              if (option.target.value === "") {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                return form.trigger(name);
              }

              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              form.setValue(name, option.target.value as any, {
                shouldValidate: true,
                shouldDirty: true,
              });
            }}
            onChange={(option) => {
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              form.setValue(name, option.target.value as any, {
                shouldValidate: true,
                shouldDirty: true,
              });
            }}
            valid={error === undefined}
            helperText={error?.message}
          />
        );
      }}
    />
  );
}

const NativeSelectMemo = memo(
  _NativeSelect,
  () => false
) as typeof _NativeSelect;

// eslint-disable-next-line import/exports-last,import/group-exports
export const NativeSelect = <T extends FieldValues>(
  props: FormInputProps<T> & BaseNativeSelectProps
): React.ReactElement<FormInputProps<T> & BaseNativeSelectProps> => {
  const ctx = useFormContext<T>();
  return <NativeSelectMemo formContext={ctx} {...props} />;
};

NativeSelect.Skeleton = BaseNativeSelect.Skeleton;

type ButtonProps = React.ComponentProps<typeof PrimaryButton>;
function _SubmitButton<T extends FieldValues>({
  formContext: {
    formState: { isSubmitting },
  },
  ...props
}: ButtonProps & FormRegisterProps<T>) {
  return <PrimaryButton {...props} loading={isSubmitting} type="submit" />;
}

const SubmitButtonMemo = memo(
  _SubmitButton,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  (_prev: FormRegisterProps, _next: FormRegisterProps) => {
    return false;
  }
) as typeof _SubmitButton;

// eslint-disable-next-line import/exports-last,import/group-exports
export const SubmitButton = <T extends FieldValues>(
  props: ButtonProps
): React.ReactElement<ButtonProps> => {
  const ctx = useFormContext<T>();
  return <SubmitButtonMemo formContext={ctx} {...props} />;
};

//
// <RadioButton>
//
function _RadioButton<T extends FieldValues>({
  name,
  formContext: form,
  ...props
}: BaseRadioButtonProps & FormInputProps<T> & FormRegisterProps<T>) {
  return <BaseRadioButton {...props} {...form.register(name)} name={name} />;
}

const RadioButtonMemo = memo(_RadioButton, () => false) as typeof _RadioButton;

// eslint-disable-next-line import/exports-last,import/group-exports
export const RadioButton = <T extends FieldValues>(
  props: FormInputProps<T> & BaseRadioButtonProps
): React.ReactElement<FormInputProps<T> & BaseRadioButtonProps> => {
  const ctx = useFormContext<T>();
  return <RadioButtonMemo formContext={ctx} {...props} />;
};

RadioButton.Skeleton = BaseRadioButton.Skeleton;

//
// <RadioButtonGroup>
//
function _RadioButtonGroup<T extends FieldValues>({
  name,
  formContext: form,
  ...props
}: BaseRadioButtonGroupProps & FormInputProps<T> & FormRegisterProps<T>) {
  return (
    <_Controller
      name={name}
      control={form.control}
      render={({ field: { value, name }, fieldState: { error } }) => {
        return (
          <BaseRadioButtonGroup
            {...props}
            name={name}
            value={value}
            onChange={(value) => {
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              form.setValue(name, value as any, {
                shouldValidate: true,
                shouldDirty: true,
              });
            }}
            error={error?.message}
            valid={error === undefined}
          />
        );
      }}
    />
  );
}

const RadioButtonGroupMemo = memo(
  _RadioButtonGroup,
  () => false
) as typeof _RadioButtonGroup;

// eslint-disable-next-line import/exports-last,import/group-exports
export const RadioButtonGroup = <T extends FieldValues>(
  props: FormInputProps<T> & BaseRadioButtonGroupProps
): React.ReactElement<FormInputProps<T> & BaseRadioButtonGroupProps> => {
  const ctx = useFormContext<T>();
  return <RadioButtonGroupMemo formContext={ctx} {...props} />;
};

RadioButtonGroup.Skeleton = BaseRadioButtonGroup.Skeleton;

function parseFieldErrorsArray<T extends FieldValues>(
  { errors }: FormState<T>,
  name: keyof T
): undefined | { index: number; message: string }[] {
  if (name in errors) {
    const fieldErrors = errors[name];

    if (fieldErrors !== undefined && Array.isArray(fieldErrors)) {
      return fieldErrors.map(
        ({ index, message }): { index: number; message: string } => ({
          index,
          message,
        })
      );
    }
  }
  return undefined;
}

function parseFieldErrorMessage<T extends FieldValues>(
  { errors }: FormState<T>,
  name: keyof T
): undefined | string {
  if (name in errors) {
    const fieldError = errors[name];
    if (fieldError !== undefined) {
      if (typeof fieldError.message === "string") {
        return fieldError.message;
      }
    }
  }
  return undefined;
}

// <ComplexNativeSelect>
//
function _ComplexNativeSelect<
  T extends FieldValues,
  FieldValue extends string | OptionType,
>({
  name,
  formContext: form,
  disabled,
  ...props
}: Omit<BaseComplexNativeSelectProps<FieldValue>, "value" | "onBlur"> &
  FormInputProps<T> &
  FormRegisterProps<T>) {
  return (
    <_Controller
      name={name}
      control={form.control}
      render={({ field: { name }, fieldState: { error } }) => {
        const { isSubmitting } = form.formState;

        return (
          <BaseComplexNativeSelect
            {...props}
            name={name}
            disabled={disabled || isSubmitting}
            onBlur={(option) => {
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              form.setValue(name, option as any, {
                shouldValidate: true,
                shouldDirty: true,
              });
            }}
            helperText={error?.message}
            valid={error ? false : undefined}
          />
        );
      }}
    />
  );
}

const ComplexNativeSelectMemo = memo(
  _ComplexNativeSelect,
  () => false
) as typeof _ComplexNativeSelect;

// eslint-disable-next-line import/exports-last,import/group-exports
export const ComplexNativeSelect = <
  T extends FieldValues,
  FieldValue extends string | OptionType,
>(
  props: FormInputProps<T> &
    Omit<BaseComplexNativeSelectProps<FieldValue>, "value" | "onBlur">
): React.ReactElement<
  FormInputProps<T> &
    Omit<BaseComplexNativeSelectProps<FieldValue>, "value" | "onBlur">
> => {
  const ctx = useFormContext<T>();
  return <ComplexNativeSelectMemo formContext={ctx} {...props} />;
};

//
// <FileUpload>
// This not part of Aiven core implementation but an input
// custom for Klaw use cases.
// Important: adding onBlur validation currently
// needs to happen on usage, not added here yet
function _FileInput<T extends FieldValues>({
  name,
  formContext: form,
  ...props
}: Omit<BaseFileInputProps, "valid" | "helperText"> &
  FormInputProps<T> &
  FormRegisterProps<T>) {
  return (
    <_Controller
      name={name}
      control={form.control}
      render={({ field: { name }, fieldState: { error } }) => {
        const { isSubmitting } = form.formState;
        return (
          <BaseFileInput
            {...props}
            name={name}
            disabled={props.disabled || isSubmitting}
            valid={!error}
            helperText={error?.message || ""}
            onChange={(event: ChangeEvent<HTMLInputElement>) => {
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              const file = error ? "" : (event.target?.files?.[0] as any);
              form.setValue(name, file, {
                shouldValidate: true,
                shouldDirty: true,
              });
            }}
          />
        );
      }}
    />
  );
}

const FileInputMemo = memo(_FileInput) as typeof _FileInput;

// eslint-disable-next-line import/exports-last,import/group-exports
export const FileInput = <T extends FieldValues>(
  props: FormInputProps<T> & Omit<BaseFileInputProps, "valid" | "helperText">
): React.ReactElement<
  FormInputProps<T> & Omit<BaseFileInputProps, "valid" | "helperText">
> => {
  const ctx = useFormContext<T>();
  return <FileInputMemo formContext={ctx} {...props} />;
};
