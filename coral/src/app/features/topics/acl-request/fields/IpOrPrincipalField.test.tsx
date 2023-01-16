import { cleanup } from "@testing-library/react";
import IpOrPrincipalField from "src/app/features/topics/acl-request/fields/IpOrPrincipalField";
import { aclIpPrincipleType } from "src/app/features/topics/acl-request/schemas/topic-acl-request-shared-fields";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const schema = z.object({
  aclIpPrincipleType,
});

describe("IpOrPrincipalField", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders a field for SSL DN strings / Usernames", () => {
    const result = renderForm(
      <IpOrPrincipalField aclIpPrincipleType={"PRINCIPAL"} />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const multiInput = result.getByLabelText("SSL DN strings / Usernames*");
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();
  });

  it("renders a field for IP addresses", () => {
    const result = renderForm(
      <IpOrPrincipalField aclIpPrincipleType={"IP_ADDRESS"} />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const multiInput = result.getByLabelText("IP addresses*");
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();
  });

  it("renders empty Box when no aclIpPrincipleType is passed", () => {
    const result = renderForm(<IpOrPrincipalField />, {
      schema,
      onSubmit,
      onError,
    });
    const empty = result.getByTestId("empty");
    expect(empty).toBeVisible();
  });
});
