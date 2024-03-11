import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import IpOrPrincipalField from "src/app/features/topics/acl-request/fields/IpOrPrincipalField";
import { aclIpPrincipleType } from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-shared-fields";
import { getAivenServiceAccounts } from "src/domain/acl/acl-api";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const schema = z.object({
  aclIpPrincipleType,
});

jest.mock("src/domain/acl/acl-api");

const mockGetAivenServiceAccounts =
  getAivenServiceAccounts as jest.MockedFunction<
    typeof getAivenServiceAccounts
  >;
const mockedAivenServiceAccountsResponse = ["bsisko", "odo", "quark"];

describe("IpOrPrincipalField", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();

  afterEach(() => {
    cleanup();
    jest.clearAllMocks();
  });

  it("renders a field for Service accounts with options for existing service accounts (Aiven cluster)", async () => {
    mockGetAivenServiceAccounts.mockResolvedValue(
      mockedAivenServiceAccountsResponse
    );

    renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"PRINCIPAL"}
        isAivenCluster={true}
        environment={"1"}
      />,
      {
        schema,
        onSubmit,
        onError,
      }
    );

    await waitForElementToBeRemoved(screen.getByTestId("acl_ssl-skeleton"));

    const multiInput = screen.getByRole("combobox", {
      name: "Service accounts *",
    });
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();

    await userEvent.click(multiInput);

    mockedAivenServiceAccountsResponse.forEach((serviceAccount) => {
      const option = screen.getByRole("option", {
        name: serviceAccount,
      });

      expect(option).toBeEnabled();
    });
  });

  it("renders a field for Service accounts which allows adding new service accounts (Aiven cluster)", async () => {
    mockGetAivenServiceAccounts.mockResolvedValue(
      mockedAivenServiceAccountsResponse
    );

    renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"PRINCIPAL"}
        isAivenCluster={true}
        environment={"1"}
      />,
      {
        schema,
        onSubmit,
        onError,
      }
    );

    await waitForElementToBeRemoved(screen.getByTestId("acl_ssl-skeleton"));

    const multiInput = screen.getByRole("combobox", {
      name: "Service accounts *",
    });
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();

    await userEvent.type(multiInput, "hello");
    await userEvent.tab();

    const newOption = screen.getByRole("button", { name: "Remove hello" });
    expect(newOption).toBeVisible();
    expect(newOption).toBeEnabled();
  });

  it("renders a textbox field for Service accounts when getAivenServiceAccounts errors (Aiven cluster)", async () => {
    jest.spyOn(console, "error").mockImplementationOnce((error) => error);

    mockGetAivenServiceAccounts.mockRejectedValueOnce("mock-error");

    renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"PRINCIPAL"}
        isAivenCluster={true}
        environment={"1"}
      />,
      {
        schema,
        onSubmit,
        onError,
      }
    );

    await waitForElementToBeRemoved(screen.getByTestId("acl_ssl-skeleton"));

    const multiInput = screen.getByRole("textbox", {
      name: "Service accounts *",
    });
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();
    expect(console.error).toHaveBeenCalledWith("mock-error");
  });

  it("renders a field for SSL DN strings / Usernames (not Aiven cluster)", () => {
    renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"PRINCIPAL"}
        isAivenCluster={false}
        environment={"1"}
      />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const multiInput = screen.getByLabelText("SSL DN strings / Usernames*");
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();
  });

  it("renders a field for IP addresses (Aiven cluster)", () => {
    renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"IP_ADDRESS"}
        isAivenCluster={true}
        environment={"1"}
      />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const multiInput = screen.getByLabelText("IP addresses*");
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();
  });

  it("renders a field for IP addresses (not Aiven cluster)", () => {
    renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"IP_ADDRESS"}
        isAivenCluster={false}
        environment={"1"}
      />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const multiInput = screen.getByLabelText("IP addresses*");
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();
  });
});
