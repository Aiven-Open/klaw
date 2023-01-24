import { cleanup } from "@testing-library/react";
import EnvironmentField from "src/app/features/topics/acl-request/fields/EnvironmentField";
import { environment } from "src/app/features/topics/acl-request/schemas/topic-acl-request-shared-fields";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const schema = z.object({
  environment,
});

const mockedEnvironments = [
  createEnvironment({
    name: "DEV",
    id: "1",
  }),
  createEnvironment({
    name: "TST",
    id: "2",
  }),
];

describe("EnvironmentField", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders NativeSelect component", () => {
    const result = renderForm(
      <EnvironmentField environments={mockedEnvironments} />,
      {
        schema,
        onSubmit,
        onError,
        defaultValues: {
          environment: undefined,
        },
      }
    );
    const select = result.getByRole("combobox");

    expect(select).toBeVisible();
    expect(select).toBeEnabled();
  });

  it("renders NativeSelect with a placeholder option", () => {
    const result = renderForm(
      <EnvironmentField environments={mockedEnvironments} />,
      {
        schema,
        onSubmit,
        onError,
        defaultValues: {
          environment: undefined,
        },
      }
    );
    const options = result.getAllByRole("option");

    expect(options).toHaveLength(mockedEnvironments.length + 1);
  });
});
