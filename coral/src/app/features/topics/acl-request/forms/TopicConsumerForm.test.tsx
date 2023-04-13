import { Context as AquariumContext } from "@aivenio/aquarium";
import {
  cleanup,
  renderHook,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import TopicConsumerForm, {
  TopicConsumerFormProps,
} from "src/app/features/topics/acl-request/forms/TopicConsumerForm";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-consumer";
import { getAivenServiceAccounts } from "src/domain/acl/acl-api";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api");

const mockGetAivenServiceAccounts =
  getAivenServiceAccounts as jest.MockedFunction<
    typeof getAivenServiceAccounts
  >;
const mockedAivenServiceAccountsResponse = {
  success: true,
  message: "success",
  data: ["bsisko", "odo", "quark"] as unknown as Record<string, never>,
};

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
} as TopicConsumerFormProps;

const basePropsIsAivenCluster = {
  ...baseProps,
  isAivenCluster: true,
} as TopicConsumerFormProps;

const basePropsNotAivenCluster = {
  ...baseProps,
  isAivenCluster: false,
} as TopicConsumerFormProps;

describe("<TopicConsumerForm />", () => {
  describe("renders correct fields in pristine form", () => {
    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            aclType: "CONSUMER",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicConsumerForm
            {...baseProps}
            topicConsumerForm={result.current}
          />
        </AquariumContext>,
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

    it("renders EnvironmentField", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Environment *",
      });

      expect(environmentField).toBeVisible();
      expect(environmentField).toBeEnabled();
    });

    it("renders TopicNameField", () => {
      const topicNameField = screen.getByRole("combobox", {
        name: "Topic name *",
      });

      expect(topicNameField).toBeVisible();
      expect(topicNameField).toBeEnabled();
      expect(topicNameField).toHaveDisplayValue("aiventopic1");
    });

    it("does not render consumergroup field", () => {
      const consumergroupField = screen.queryByRole("textbox", {
        name: "Consumer group *",
      });

      expect(consumergroupField).toBeNull();
    });

    it("renders TopicNameField", () => {
      const topicNameField = screen.getByRole("combobox", {
        name: "Topic name *",
      });

      expect(topicNameField).toBeVisible();
      expect(topicNameField).toBeEnabled();
      expect(topicNameField).toHaveDisplayValue("aiventopic1");
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
      const ipsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });
      const principalsField = screen.queryByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(ipsField).toBeNull();
      expect(principalsField).toBeNull();
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
      expect(submitButton).not.toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });

  // The action controlling the form states affected by selecting an environment is controlled by TopicAclRequest
  // We mock it by passing the values of the form that would have changed as default values
  describe("renders correct fields in form with selected environment which is Aiven cluster", () => {
    beforeAll(() => {
      mockGetAivenServiceAccounts.mockResolvedValue(
        mockedAivenServiceAccountsResponse
      );

      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "1",
            aclType: "CONSUMER",
            aclIpPrincipleType: "PRINCIPAL",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicConsumerForm
            {...basePropsIsAivenCluster}
            topicConsumerForm={result.current}
          />
        </AquariumContext>,
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

    it("renders TopicNameField", () => {
      const topicNameField = screen.getByRole("combobox", {
        name: "Topic name *",
      });

      expect(topicNameField).toBeVisible();
      expect(topicNameField).toBeEnabled();
      expect(topicNameField).toHaveDisplayValue("aiventopic1");
    });

    it("does not render consumergroup field", () => {
      const consumergroupField = screen.queryByRole("textbox", {
        name: "Consumer group *",
      });

      expect(consumergroupField).toBeNull();
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
      expect(submitButton).not.toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });

  // The action controlling the form states affected by selecting an environment is controlled by TopicAclRequest
  // We mock it by passing the values of the form that would have changed as default values
  describe("renders correct fields in form with selected environment which is NOT Aiven cluster", () => {
    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "2",
            aclType: "CONSUMER",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicConsumerForm
            {...basePropsNotAivenCluster}
            topicConsumerForm={result.current}
          />
        </AquariumContext>,
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

    it("renders TopicNameField", () => {
      const topicNameField = screen.getByRole("combobox", {
        name: "Topic name *",
      });

      expect(topicNameField).toBeVisible();
      expect(topicNameField).toBeEnabled();
      expect(topicNameField).toHaveDisplayValue("aiventopic1");
    });

    it("renders consumergroup field", () => {
      const consumergroupField = screen.getByRole("textbox", {
        name: "Consumer group *",
      });

      expect(consumergroupField).toBeVisible();
      expect(consumergroupField).toBeEnabled();
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
      const ipsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });
      const principalsField = screen.queryByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(ipsField).toBeNull();
      expect(principalsField).toBeNull();
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
      expect(submitButton).not.toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });
});
