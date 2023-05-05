import { Context as AquariumContext } from "@aivenio/aquarium";
import {
  cleanup,
  renderHook,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-consumer";
import TopicConsumerForm, {
  TopicConsumerFormProps,
} from "src/app/features/topics/acl-request/forms/TopicConsumerForm";
import { ExtendedEnvironment } from "src/app/features/topics/acl-request/queries/useExtendedEnvironments";
import { getAivenServiceAccounts } from "src/domain/acl/acl-api";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api");

const mockGetAivenServiceAccounts =
  getAivenServiceAccounts as jest.MockedFunction<
    typeof getAivenServiceAccounts
  >;
const mockedAivenServiceAccountsResponse = ["bsisko", "odo", "quark"];

const mockedEnvironments: ExtendedEnvironment[] = [
  {
    name: "DEV",
    id: "1",
    topicNames: ["hello", "there"],
    isAivenCluster: true,
    type: "kafka",
  },
  {
    name: "TST",
    id: "2",
    topicNames: ["hello", "there", "general"],
    isAivenCluster: false,
    type: "kafka",
  },
  {
    name: "NOTOPIC",
    id: "3",
    topicNames: [],
    isAivenCluster: true,
    type: "kafka",
  },
];

const baseProps = {
  topicNames: [] as string[],
  environments: mockedEnvironments,
  renderAclTypeField: () => (
    <AclTypeField aclType={"PRODUCER"} handleChange={() => null} />
  ),
  isSubscription: false,
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

    it("renders required EnvironmentField", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Environment *",
      });

      expect(environmentField).toBeVisible();
      expect(environmentField).toBeEnabled();
      expect(environmentField).toBeRequired();
    });

    it("renders required TopicNameField", () => {
      const topicNameField = screen.getByRole("combobox", {
        name: "Topic name *",
      });

      expect(topicNameField).toBeVisible();
      expect(topicNameField).toBeEnabled();
      expect(topicNameField).toBeRequired();
      expect(topicNameField).toHaveDisplayValue("-- Select Topic --");
    });

    it("does not render consumergroup field", () => {
      const consumergroupField = screen.queryByRole("textbox", {
        name: "Consumer group *",
      });

      expect(consumergroupField).toBeNull();
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

    it("renders enabled Submit and Cancel buttons", () => {
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
    beforeAll(() => {
      mockGetAivenServiceAccounts.mockResolvedValue(
        mockedAivenServiceAccountsResponse
      );

      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
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
      expect(topicNameField).toHaveDisplayValue("-- Select Topic --");
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
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
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
      expect(topicNameField).toHaveDisplayValue("-- Select Topic --");
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
      expect(submitButton).toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });
});

const isSubscriptionBaseProps = {
  ...baseProps,
  isSubscription: true,
} as TopicConsumerFormProps;

const isSubscriptionBasePropsIsAivenCluster = {
  ...isSubscriptionBaseProps,
  isAivenCluster: true,
} as TopicConsumerFormProps;

const isSubscriptionBasePropsNotAivenCluster = {
  ...isSubscriptionBaseProps,
  isAivenCluster: false,
} as TopicConsumerFormProps;

describe("<TopicConsumerForm isSubscription />", () => {
  describe("renders correct fields in pristine form", () => {
    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            aclType: "CONSUMER",
            topicname: "hello",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicConsumerForm
            {...isSubscriptionBaseProps}
            topicConsumerForm={result.current}
            topicNames={["hello"]}
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

    it("renders required EnvironmentField", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Environment *",
      });

      expect(environmentField).toBeVisible();
      expect(environmentField).toBeEnabled();
      expect(environmentField).toBeRequired();
    });

    it("renders required readonly TopicNameField", () => {
      const topicNameField = screen.getByRole("combobox", {
        name: "Topic name *",
      });

      expect(topicNameField).toBeVisible();
      expect(topicNameField).toHaveAttribute("aria-readOnly", "true");
      expect(topicNameField).toBeRequired();
      expect(topicNameField).toHaveDisplayValue("hello");
    });

    it("does not render consumergroup field", () => {
      const consumergroupField = screen.queryByRole("textbox", {
        name: "Consumer group *",
      });

      expect(consumergroupField).toBeNull();
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

    it("renders enabled Submit and Cancel buttons", () => {
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
    beforeAll(() => {
      mockGetAivenServiceAccounts.mockResolvedValue(
        mockedAivenServiceAccountsResponse
      );

      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            environment: "1",
            aclType: "CONSUMER",
            aclIpPrincipleType: "PRINCIPAL",
            topicname: "hello",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicConsumerForm
            {...isSubscriptionBasePropsIsAivenCluster}
            topicNames={["hello"]}
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
      expect(topicNameField).toHaveAttribute("aria-readOnly", "true");
      expect(topicNameField).toBeRequired();
      expect(topicNameField).toHaveValue("hello");
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
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            environment: "2",
            aclType: "CONSUMER",
            topicname: "hello",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicConsumerForm
            {...isSubscriptionBasePropsNotAivenCluster}
            topicConsumerForm={result.current}
            topicNames={["hello"]}
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
      expect(topicNameField).toHaveAttribute("aria-readOnly", "true");
      expect(topicNameField).toBeRequired();
      expect(topicNameField).toHaveValue("hello");
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
      expect(submitButton).toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });
});
