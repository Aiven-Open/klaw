import { cleanup } from "@testing-library/react";
import AclIpPrincipleTypeField from "src/app/features/topics/acl-request/fields/AclIpPrincipleTypeField";
import { aclIpPrincipleType } from "src/app/features/topics/acl-request/schemas/topic-acl-request-shared-fields";
import { ClusterInfo } from "src/domain/environment";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

const clusterInfoIsAiven: ClusterInfo = {
  aivenCluster: "true",
};
const clusterInfoIsNotAiven: ClusterInfo = {
  aivenCluster: "false",
};
const clusterInfoNotFetched = undefined;

const schema = z.object({
  aclIpPrincipleType,
});

describe("AclIpPrincipleTypeField", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders AclIpPrincipleTypeField with two options", () => {
    const result = renderForm(
      <AclIpPrincipleTypeField clusterInfo={clusterInfoIsNotAiven} />,
      { schema, onSubmit, onError }
    );
    const radioButtons = result.getAllByRole("radio");
    expect(radioButtons).toHaveLength(2);
  });

  it("should render AclIpPrincipleTypeField with no option checked by default", () => {
    const result = renderForm(
      <AclIpPrincipleTypeField clusterInfo={clusterInfoIsNotAiven} />,
      { schema, onSubmit, onError }
    );
    const radioButtons = result.getAllByRole("radio");
    radioButtons.forEach((radio) => expect(radio).not.toBeChecked());
  });

  it("renders AclIpPrincipleTypeField with both options enabled", () => {
    const result = renderForm(
      <AclIpPrincipleTypeField clusterInfo={clusterInfoIsNotAiven} />,
      { schema, onSubmit, onError }
    );
    const radioButtons = result.getAllByRole("radio");
    radioButtons.forEach((radio) => expect(radio).toBeEnabled());
  });

  it("renders AclIpPrincipleTypeField with only Principal option enabled", () => {
    const result = renderForm(
      <AclIpPrincipleTypeField clusterInfo={clusterInfoIsAiven} />,
      { schema, onSubmit, onError }
    );
    const principalRadioButtons = result.getByLabelText("Principal");
    const ipRadioButtons = result.getByLabelText("IP");

    expect(principalRadioButtons).toBeEnabled();
    expect(ipRadioButtons).not.toBeEnabled();
  });

  it("renders AclIpPrincipleTypeField with both options disabled", () => {
    const result = renderForm(
      <AclIpPrincipleTypeField clusterInfo={clusterInfoNotFetched} />,
      { schema, onSubmit, onError }
    );
    const principalRadioButtons = result.getByLabelText("Principal");
    const ipRadioButtons = result.getByLabelText("IP");

    expect(principalRadioButtons).not.toBeEnabled();
    expect(ipRadioButtons).not.toBeEnabled();
  });
});
