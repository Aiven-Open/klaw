import { Button } from "@aivenio/aquarium";
import React from "react";
import type { DeepPartial, FieldValues } from "react-hook-form";
import {
  Form,
  SubmitButton,
  SubmitErrorHandler,
  SubmitHandler,
  useForm,
} from "src/app/components/Form";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { ZodSchema } from "zod";
import { render } from "@testing-library/react";

type WrapperProps<T extends FieldValues> = {
  schema: ZodSchema;
  defaultValues?: DeepPartial<T>;
  onSubmit: SubmitHandler<T>;
  onError: SubmitErrorHandler<T>;
};

const Wrapper = <T extends FieldValues>({
  schema,
  defaultValues,
  onSubmit,
  onError,
  children,
}: React.PropsWithChildren<WrapperProps<T>>): React.ReactElement => {
  const form = useForm<T>({ schema, defaultValues });
  return (
    <Form onSubmit={onSubmit} onError={onError} {...form}>
      {children}
    </Form>
  );
};

// eslint-disable-next-line import/group-exports
export const renderForm = <T extends FieldValues>(
  children: React.ReactNode,
  {
    schema,
    defaultValues,
    onSubmit,
    onError,
  }: {
    schema: ZodSchema;
    defaultValues?: DeepPartial<T>;
    onSubmit: SubmitHandler<T>;
    onError: SubmitErrorHandler<T>;
  }
) => {
  return customRender(
    <Wrapper<T>
      schema={schema}
      defaultValues={defaultValues}
      onSubmit={onSubmit}
      onError={onError}
    >
      {children}
      <Button type="submit" title="Submit" />
    </Wrapper>,
    { queryClient: true }
  );
};

// eslint-disable-next-line import/group-exports
export const renderFormWithState = <T extends FieldValues>(
  children: React.ReactNode,
  {
    isLoading,
    schema,
    defaultValues,
    onSubmit,
    onError,
  }: {
    isLoading: boolean;
    schema: ZodSchema;
    defaultValues?: DeepPartial<T>;
    onSubmit: SubmitHandler<T>;
    onError: SubmitErrorHandler<T>;
  }
) => {
  return render(
    <Wrapper<T>
      schema={schema}
      defaultValues={defaultValues}
      onSubmit={onSubmit}
      onError={onError}
    >
      {children}
      <SubmitButton title="Submit" loading={isLoading}>
        Submit
      </SubmitButton>
    </Wrapper>
  );
};
