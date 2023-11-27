import { cleanup, screen, within } from "@testing-library/react";
import EnvironmentField from "src/app/features/topics/acl-request/fields/EnvironmentField";
import { environment } from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-shared-fields";
import { ExtendedEnvironment } from "src/app/features/topics/acl-request/queries/useExtendedEnvironments";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const schema = z.object({
  environment,
});

const mockedEnvironments: ExtendedEnvironment[] = [
  {
    ...createMockEnvironmentDTO({
      name: "DEV",
      id: "1",
      type: "kafka",
      clusterName: "DEV",
      tenantName: "default",
      envStatus: "ONLINE",
      envStatusTimeString: "21-Sep-2023 11:46:15",
    }),
    topicNames: ["hello", "there"],
    isAivenCluster: true,
  },
  {
    ...createMockEnvironmentDTO({
      name: "TST",
      id: "2",
      type: "kafka",
      clusterName: "DEV",
      tenantName: "default",
      envStatus: "ONLINE",
      envStatusTimeString: "21-Sep-2023 11:46:15",
    }),
    topicNames: ["hello", "there", "general"],
    isAivenCluster: true,
  },
  {
    ...createMockEnvironmentDTO({
      name: "NOTOPIC",
      id: "3",
      type: "kafka",
      clusterName: "DEV",
      tenantName: "default",
      envStatus: "ONLINE",
      envStatusTimeString: "21-Sep-2023 11:46:15",
    }),
    topicNames: [],
    isAivenCluster: true,
  },
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
    renderForm(<EnvironmentField environments={mockedEnvironments} />, {
      schema,
      onSubmit,
      onError,
    });
    const select = screen.getByRole("combobox", {
      name: "Environment *",
    });

    expect(select).toBeVisible();
    expect(select).toBeEnabled();
    expect(select).toBeRequired();
  });

  it("renders a readOnly NativeSelect component", () => {
    renderForm(
      <EnvironmentField environments={mockedEnvironments} readOnly={true} />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const select = screen.getByRole("combobox", {
      name: "Environment (read-only)",
    });

    expect(select).toBeVisible();
    expect(select).toBeDisabled();
    expect(select).toHaveAttribute("aria-readonly", "true");
  });

  it("renders NativeSelect with a placeholder option", () => {
    renderForm(<EnvironmentField environments={mockedEnvironments} />, {
      schema,
      onSubmit,
      onError,
    });
    const select = screen.getByRole("combobox", {
      name: "Environment *",
    });
    const options = within(select).getAllByRole("option");

    expect(select).toHaveDisplayValue("-- Please select --");
    expect(options).toHaveLength(mockedEnvironments.length + 1);
  });

  it("renders disabled option if an environment has no topics", () => {
    renderForm(<EnvironmentField environments={mockedEnvironments} />, {
      schema,
      onSubmit,
      onError,
    });
    const select = screen.getByRole("combobox", {
      name: "Environment *",
    });
    const disabledOption = within(select).getByRole("option", {
      name: "NOTOPIC (no topic available)",
    });

    expect(disabledOption).toBeDisabled();
  });
});
