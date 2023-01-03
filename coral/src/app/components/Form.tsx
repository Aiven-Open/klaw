import {
  PrimaryButton,
  Input as BaseInput,
  InputProps as BaseInputProps,
  Option,
} from "@aivenio/aquarium";
import { zodResolver } from "@hookform/resolvers/zod";
import React, { memo } from "react";
import {
  FieldError,
  FieldPath,
  FieldValues,
  FormProvider,
  SubmitErrorHandler,
  SubmitHandler,
  useForm as _useForm,
  useWatch,
  useFormContext,
  UseFormProps as _UseFormProps,
  UseFormReturn,
} from "react-hook-form";
import type { FormState } from "react-hook-form";
import { ZodSchema } from "zod";
import get from "lodash/get";

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
    mode: "onBlur",
    defaultValues,
    resolver: schema && zodResolver(schema),
  });
};

type FormProps<T extends FieldValues = FieldValues> = UseFormReturn<T> & {
  onSubmit: SubmitHandler<T>;
  onError?: SubmitErrorHandler<T>;
};

// eslint-disable-next-line import/exports-last,import/group-exports
export const Form = <T extends FieldValues = FieldValues>({
  onSubmit,
  onError,
  children,
  ...form
}: React.PropsWithChildren<FormProps<T>>): React.ReactElement<FormProps<T>> => {
  return (
    <FormProvider {...form}>
      <form onSubmit={form.handleSubmit(onSubmit, onError)}>{children}</form>
    </FormProvider>
  );
};

//
// <PasswordInput>
// This not part of Aiven core implementation but an input
// custom for Klaw use cases. It's exactly the same as <TextInput>
// with the only difference being the type (password) to have
// a more secure way for users to enter password (input obscured)
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

type ButtonProps = React.ComponentProps<typeof PrimaryButton>;

function _SubmitButton<T extends FieldValues>({
  formContext: {
    formState: { isDirty, isValid },
  },
  ...props
}: ButtonProps & FormRegisterProps<T>) {
  return (
    <PrimaryButton {...props} type="submit" disabled={!isDirty || !isValid} />
  );
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

function parseFieldErrorMessage<T extends FieldValues>(
  { errors }: FormState<T>,
  name: keyof T
): undefined | string {
  if (name in errors) {
    const fieldError = errors.name;
    if (fieldError !== undefined) {
      if (typeof fieldError.message === "string") {
        return fieldError.message;
      }
    }
  }
  return undefined;
}
