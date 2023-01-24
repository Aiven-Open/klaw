import { cleanup } from "@testing-library/react";
import IpOrPrincipalField from "src/app/features/topics/acl-request/fields/IpOrPrincipalField";
import { aclIpPrincipleType } from "src/app/features/topics/acl-request/schemas/topic-acl-request-shared-fields";
import { ClusterInfo } from "src/domain/environment";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const schema = z.object({
  aclIpPrincipleType,
});

const isAivenClusterInfo: ClusterInfo = {
  aivenCluster: "true",
};

const isNotAivenClusterInfo: ClusterInfo = {
  aivenCluster: "false",
};

describe("IpOrPrincipalField", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders a field for Service Accounts (Aiven cluster)", () => {
    const result = renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"PRINCIPAL"}
        clusterInfo={isAivenClusterInfo}
      />,
      {
        schema,
        onSubmit,
        onError,
      }
    );
    const multiInput = result.getByLabelText("Service Accounts*");
    expect(multiInput).toBeVisible();
    expect(multiInput).toBeEnabled();
  });

  it("renders a field for SSL DN strings / Usernames (not Aiven cluster)", () => {
    const result = renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"PRINCIPAL"}
        clusterInfo={isNotAivenClusterInfo}
      />,
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

  it("renders a field for IP addresses (Aiven cluster)", () => {
    const result = renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"IP_ADDRESS"}
        clusterInfo={isAivenClusterInfo}
      />,
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

  it("renders a field for IP addresses (not Aiven cluster)", () => {
    const result = renderForm(
      <IpOrPrincipalField
        aclIpPrincipleType={"IP_ADDRESS"}
        clusterInfo={isNotAivenClusterInfo}
      />,
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
});
