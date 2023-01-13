import { Button } from "@aivenio/aquarium";
import { render } from "@testing-library/react";
import React from "react";
import type { DeepPartial, FieldValues } from "react-hook-form";
import {
  Form,
  SubmitErrorHandler,
  SubmitHandler,
  useForm,
} from "src/app/components/Form";
import { ZodSchema } from "zod";

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
  return render(
    <Wrapper<T>
      schema={schema}
      defaultValues={defaultValues}
      onSubmit={onSubmit}
      onError={onError}
    >
      {children}
      <Button type="submit" title="Submit" />
    </Wrapper>
  );
};
