import {
  cleanup,
  renderHook,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import TopicProducerForm, {
  TopicProducerFormProps,
} from "src/app/features/topics/acl-request/forms/TopicProducerForm";
import topicProducerFormSchema, {
  TopicProducerFormSchema,
} from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-producer";
import { getAivenServiceAccounts } from "src/domain/acl/acl-api";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { ENVIRONMENT_NOT_INITIALIZED } from "src/domain/environment/environment-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api");

const mockGetAivenServiceAccounts =
  getAivenServiceAccounts as jest.MockedFunction<
    typeof getAivenServiceAccounts
  >;
const mockedAivenServiceAccountsResponse = ["bsisko", "odo", "quark"];

const baseProps = {
  topicNames: ["aiventopic1", "aiventopic2", "othertopic"],
  environments: [
    createEnvironment({
      name: "DEV",
      id: "1",
    }),
    createEnvironment({
      name: "TST",
      id: "2",
    }),
  ],
  renderAclTypeField: () => (
    <AclTypeField aclType={"PRODUCER"} handleChange={() => null} />
  ),
} as TopicProducerFormProps;

const basePropsIsAivenCluster = {
  ...baseProps,
  isAivenCluster: true,
} as TopicProducerFormProps;

const basePropsNotAivenCluster = {
  ...baseProps,
  isAivenCluster: false,
} as TopicProducerFormProps;

describe("<TopicProducerForm />", () => {
  describe("renders correct fields in pristine form", () => {
    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: ENVIRONMENT_NOT_INITIALIZED,
            aclType: "PRODUCER",
          },
        })
      );
      customRender(
        <TopicProducerForm {...baseProps} topicProducerForm={result.current} />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
    });

    it("renders AclTypeField", () => {
      const producerField = screen.getByRole("radio", { name: "Producer" });
      const consumerField = screen.getByRole("radio", {
        name: "Consumer",
      });

      expect(producerField).toBeVisible();
      expect(producerField).toBeEnabled();
      expect(consumerField).toBeVisible();
      expect(consumerField).toBeEnabled();
    });

    it("renders required EnvironmentField", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Environment *",
      });

      expect(environmentField).toBeVisible();
      expect(environmentField).toBeEnabled();
      expect(environmentField).toBeRequired();
    });

    it("renders aclPatternType field", () => {
      const literalField = screen.getByRole("radio", { name: "Literal" });
      const prefixedField = screen.getByRole("radio", {
        name: "Prefixed",
      });

      expect(literalField).toBeVisible();
      expect(literalField).toBeEnabled();
      expect(prefixedField).toBeVisible();
      expect(prefixedField).toBeEnabled();
    });

    it("does not render TopicNameOrPrefixField", () => {
      const topicNameField = screen.queryByRole("combobox", {
        name: "Topic name *",
      });
      const prefixField = screen.queryByRole("textbox", {
        name: "Prefix",
      });

      expect(topicNameField).toBeNull();
      expect(prefixField).toBeNull();
    });

    it("renders transactionalId field", () => {
      const transactionalIdField = screen.queryByRole("textbox", {
        name: "Transactional ID",
      });

      expect(transactionalIdField).toBeVisible();
      expect(transactionalIdField).toBeEnabled();
    });

    it("renders AclIpPrincipleTypeField", () => {
      const ipField = screen.getByRole("radio", { name: "IP" });
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });

      expect(ipField).toBeVisible();
      expect(ipField).not.toBeEnabled();
      expect(principalField).toBeVisible();
      expect(principalField).not.toBeEnabled();
    });

    it("does not render IpOrPrincipalField", () => {
      const hiddenIpsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });
      const hiddenPrincipalsField = screen.queryByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(hiddenIpsField).toBeNull();
      expect(hiddenPrincipalsField).toBeNull();
    });

    it("renders RemarksField", () => {
      const remarksField = screen.getByRole("textbox", { name: "Remarks" });

      expect(remarksField).toBeVisible();
      expect(remarksField).toBeEnabled();
    });

    it("renders Submit and Cancel buttons", () => {
      const submitButton = screen.getByRole("button", {
        name: "Submit request",
      });
      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      expect(submitButton).toBeVisible();
      expect(submitButton).toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });

  // The action controlling the form states affected by selecting an environment is controlled by TopicAclRequest
  // We mock it by passing the values of the form that would have changed as default values
  describe("renders correct fields in form with selected environment which is Aiven cluster", () => {
    beforeAll(async () => {
      mockGetAivenServiceAccounts.mockResolvedValue(
        mockedAivenServiceAccountsResponse
      );

      const { result } = renderHook(() =>
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "1",
            aclType: "PRODUCER",
            aclIpPrincipleType: "PRINCIPAL",
          },
        })
      );

      customRender(
        <TopicProducerForm
          {...basePropsIsAivenCluster}
          topicProducerForm={result.current}
        />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
    });

    it("renders AclTypeField", () => {
      const producerField = screen.getByRole("radio", { name: "Producer" });
      const consumerField = screen.getByRole("radio", {
        name: "Consumer",
      });

      expect(producerField).toBeVisible();
      expect(producerField).toBeEnabled();
      expect(consumerField).toBeVisible();
      expect(consumerField).toBeEnabled();
    });

    it("renders EnvironmentField with DEV selected", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Environment *",
      });

      expect(environmentField).toBeVisible();
      expect(environmentField).toBeEnabled();
      expect(environmentField).toHaveDisplayValue("DEV");
    });

    it("renders aclPatternType field with Literal option disabled", () => {
      const literalField = screen.getByRole("radio", { name: "Literal" });
      const prefixedField = screen.getByRole("radio", {
        name: "Prefixed",
      });

      expect(literalField).toBeVisible();
      expect(literalField).not.toBeEnabled();
      expect(prefixedField).toBeVisible();
      expect(prefixedField).not.toBeEnabled();
    });

    it("does not render TopicNameOrPrefixField", () => {
      const topicNameField = screen.queryByRole("combobox", {
        name: "Topic name *",
      });
      const prefixField = screen.queryByRole("textbox", {
        name: "Prefix",
      });

      expect(topicNameField).toBeNull();
      expect(prefixField).toBeNull();
    });

    it("does not render transactionalId field", () => {
      const transactionalIdField = screen.queryByRole("textbox", {
        name: "Transactional ID",
      });

      expect(transactionalIdField).toBeNull();
    });

    it("renders AclIpPrincipleTypeField with fields disabled and Principal field checked", () => {
      const ipField = screen.getByRole("radio", { name: "IP" });
      const principalField = screen.getByRole("radio", {
        name: "Service account",
      });

      expect(ipField).toBeVisible();
      expect(ipField).not.toBeEnabled();
      expect(principalField).toBeVisible();
      expect(principalField).not.toBeEnabled();
      expect(principalField).toBeChecked();
    });

    it("renders only Service accounts field in IpOrPrincipalField", async () => {
      await waitForElementToBeRemoved(screen.getByTestId("acl_ssl-skeleton"));

      const hiddenIpsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });

      const serviceAccountsField = screen.getByRole("combobox", {
        name: "Service accounts *",
      });

      expect(hiddenIpsField).toBeNull();
      expect(serviceAccountsField).toBeVisible();
      expect(serviceAccountsField).toBeEnabled();
    });

    it("renders RemarksField", () => {
      const remarksField = screen.getByRole("textbox", { name: "Remarks" });

      expect(remarksField).toBeVisible();
      expect(remarksField).toBeEnabled();
    });

    it("renders Submit and Cancel buttons", () => {
      const submitButton = screen.getByRole("button", {
        name: "Submit request",
      });
      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      expect(submitButton).toBeVisible();
      expect(submitButton).toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });

  // The action controlling the form states affected by selecting an environment is controlled by TopicAclRequest
  // We mock it by passing the values of the form that would have changed as default values
  describe("renders correct fields in form with selected environment which is NOT Aiven cluster", () => {
    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "2",
            aclType: "PRODUCER",
          },
        })
      );
      customRender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
    });

    it("renders AclTypeField", () => {
      const producerField = screen.getByRole("radio", { name: "Producer" });
      const consumerField = screen.getByRole("radio", {
        name: "Consumer",
      });

      expect(producerField).toBeVisible();
      expect(producerField).toBeEnabled();
      expect(consumerField).toBeVisible();
      expect(consumerField).toBeEnabled();
    });

    it("renders EnvironmentField with TST selected", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Environment *",
      });

      expect(environmentField).toBeVisible();
      expect(environmentField).toBeEnabled();
      expect(environmentField).toHaveDisplayValue("TST");
    });

    it("renders aclPatternType field", () => {
      const literalField = screen.getByRole("radio", { name: "Literal" });
      const prefixedField = screen.getByRole("radio", {
        name: "Prefixed",
      });

      expect(literalField).toBeVisible();
      expect(literalField).toBeEnabled();
      expect(prefixedField).toBeVisible();
      expect(prefixedField).toBeEnabled();
    });

    it("does not render TopicNameOrPrefixField", () => {
      const topicNameField = screen.queryByRole("combobox", {
        name: "Topic name *",
      });
      const prefixField = screen.queryByRole("textbox", {
        name: "Prefix",
      });

      expect(topicNameField).toBeNull();
      expect(prefixField).toBeNull();
    });

    it("renders transactionalId field", () => {
      const transactionalIdField = screen.queryByRole("textbox", {
        name: "Transactional ID",
      });

      expect(transactionalIdField).toBeVisible();
      expect(transactionalIdField).toBeEnabled();
    });

    it("renders AclIpPrincipleTypeField with IP and Principal field enabled and unchecked", () => {
      const ipField = screen.getByRole("radio", { name: "IP" });
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });

      expect(ipField).toBeVisible();
      expect(ipField).toBeEnabled();
      expect(ipField).not.toBeChecked();
      expect(principalField).toBeVisible();
      expect(principalField).toBeEnabled();
      expect(principalField).not.toBeChecked();
    });

    it("renders only principals field in IpOrPrincipalField", () => {
      const hiddenIpsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });
      const hiddenPrincipalsField = screen.queryByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(hiddenIpsField).toBeNull();
      expect(hiddenPrincipalsField).toBeNull();
    });

    it("renders RemarksField", () => {
      const remarksField = screen.getByRole("textbox", { name: "Remarks" });

      expect(remarksField).toBeVisible();
      expect(remarksField).toBeEnabled();
    });

    it("renders Submit and Cancel buttons", () => {
      const submitButton = screen.getByRole("button", {
        name: "Submit request",
      });
      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      expect(submitButton).toBeVisible();
      expect(submitButton).toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });
});
