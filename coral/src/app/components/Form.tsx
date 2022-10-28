// ❗️ This implementation mirrors the Aiven core and is kept up to date with it.
// The Aiven core solution will be its own open source package soon.
// we're mirroring the implementation as it is, using only the components we actively use in coral.
// ❗️The Aiven core code base is the source of truth for this component.
import {
  PrimaryButton,
  Input as BaseInput,
  InputProps as BaseInputProps,
  Option,
} from "@aivenio/design-system";
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
import { ZodSchema } from "zod";
import get from "lodash/get";

type FormInputProps<T extends FieldValues = FieldValues> = {
  name: FieldPath<T>;
};

type FormRegisterProps<T extends FieldValues = FieldValues> = {
  formContext: UseFormReturn<T>;
};

export type { SubmitHandler, SubmitErrorHandler, FieldError };

export { useWatch, Option };

type UseFormProps<T extends FieldValues = FieldValues> = Omit<
  _UseFormProps<T>,
  "resolver"
> & {
  schema?: ZodSchema;
};

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

export const TextInput = <T extends FieldValues>(
  props: FormInputProps<T> & BaseInputProps
): React.ReactElement<FormInputProps<T> & BaseInputProps> => {
  const ctx = useFormContext<T>();
  return <TextInputMemo formContext={ctx} {...props} />;
};

TextInput.Skeleton = BaseInput.Skeleton;

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

export const SubmitButton = <T extends FieldValues>(
  props: ButtonProps
): React.ReactElement<ButtonProps> => {
  const ctx = useFormContext<T>();
  return <SubmitButtonMemo formContext={ctx} {...props} />;
};
