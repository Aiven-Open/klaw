import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, renderHook, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import TopicConsumerForm, {
  TopicConsumerFormProps,
} from "src/app/features/topics/acl-request/forms/TopicConsumerForm";
import topicConsumerFormSchema, {
  TopicConsumerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-consumer";
import { createEnvironment } from "src/domain/environment/environment-test-helper";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const baseProps = {
  topicNames: ["aiventopic1", "aiventopic2", "othertopic"],
  topicTeam: "ospo",
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
    <AclTypeField topicType={"Producer"} handleChange={() => null} />
  ),
} as TopicConsumerFormProps;

const basePropsIsAivenCluster = {
  topicNames: ["aiventopic1", "aiventopic2", "othertopic"],
  topicTeam: "ospo",
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
    <AclTypeField topicType={"Producer"} handleChange={() => null} />
  ),
  clusterInfo: { aivenCluster: "true" },
} as TopicConsumerFormProps;

const basePropsNotAivenCluster = {
  topicNames: ["aiventopic1", "aiventopic2", "othertopic"],
  topicTeam: "ospo",
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
    <AclTypeField topicType={"Producer"} handleChange={() => null} />
  ),
  clusterInfo: { aivenCluster: "false" },
} as TopicConsumerFormProps;

describe("<TopicAclRequest />", () => {
  describe("renders correct fields in pristine form", () => {
    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "placeholder",
            topictype: "Consumer",
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
        { queryClient: true }
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
      expect(producerField).toBeVisible();
      expect(consumerField).toBeEnabled();
      expect(consumerField).toBeEnabled();
    });

    it("renders EnvironmentField", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
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

    it("renders consumergroup field", () => {
      const consumergroupField = screen.getByRole("textbox", {
        name: "Consumer group *",
      });

      expect(consumergroupField).toBeVisible();
      expect(consumergroupField).toBeEnabled();
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
      const submitButton = screen.getByRole("button", { name: "Submit" });
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
      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "1",
            topictype: "Consumer",
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
        { queryClient: true }
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
      expect(producerField).toBeVisible();
      expect(consumerField).toBeEnabled();
      expect(consumerField).toBeEnabled();
    });

    it("renders EnvironmentField with DEV selected", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
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

    it("renders consumergroup field", () => {
      const consumergroupField = screen.getByRole("textbox", {
        name: "Consumer group *",
      });

      expect(consumergroupField).toBeVisible();
      expect(consumergroupField).toBeEnabled();
    });

    it("renders AclIpPrincipleTypeField with IP field disabled and Principal field enabled and checked", () => {
      const ipField = screen.getByRole("radio", { name: "IP" });
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });

      expect(ipField).toBeVisible();
      expect(ipField).not.toBeEnabled();
      expect(principalField).toBeVisible();
      expect(principalField).toBeEnabled();
      expect(principalField).toBeChecked();
    });

    it("renders only principals field in IpOrPrincipalField", () => {
      const ipsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });
      const principalsField = screen.getByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(ipsField).toBeNull();
      expect(principalsField).toBeVisible();
      expect(principalsField).toBeEnabled();
    });

    it("renders RemarksField", () => {
      const remarksField = screen.getByRole("textbox", { name: "Remarks" });

      expect(remarksField).toBeVisible();
      expect(remarksField).toBeEnabled();
    });

    it("renders Submit and Cancel buttons", () => {
      const submitButton = screen.getByRole("button", { name: "Submit" });
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
            topictype: "Consumer",
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
        { queryClient: true }
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
      expect(producerField).toBeVisible();
      expect(consumerField).toBeEnabled();
      expect(consumerField).toBeEnabled();
    });

    it("renders EnvironmentField with TST selected", () => {
      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
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
      const submitButton = screen.getByRole("button", { name: "Submit" });
      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      expect(submitButton).toBeVisible();
      expect(submitButton).not.toBeEnabled();
      expect(cancelButton).toBeVisible();
      expect(cancelButton).toBeEnabled();
    });
  });

  describe("AclIpPrincipleTypeField and IpOrPrincipalField", () => {
    let user: ReturnType<typeof userEvent.setup>;

    beforeEach(() => {
      user = userEvent.setup();
    });

    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "2",
            topictype: "Consumer",
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
        { queryClient: true }
      );
    });

    afterAll(() => {
      cleanup();
    });

    it("renders correct fields when selecting IP or Principal in AclIpPrincipleTypeField", async () => {
      const ipField = screen.getByRole("radio", { name: "IP" });
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });
      const ipsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });
      const principalsField = screen.queryByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(ipField).toBeVisible();
      expect(ipField).toBeEnabled();
      expect(ipField).not.toBeChecked();
      expect(principalField).toBeVisible();
      expect(principalField).toBeEnabled();
      expect(principalField).not.toBeChecked();
      expect(ipsField).toBeNull();
      expect(principalsField).toBeNull();

      await user.click(ipField);
      expect(ipField).toBeChecked();
      waitFor(() => {
        expect(ipsField).toBeInTheDocument();
        expect(ipsField).toBeEnabled();
      });

      await user.click(principalField);
      expect(principalField).toBeChecked();
      waitFor(() => {
        expect(principalsField).toBeInTheDocument();
        expect(principalsField).toBeEnabled();
      });
    });

    it("error when entering invalid IP in IPs field", async () => {
      const ipField = screen.getByRole("radio", { name: "IP" });

      await user.click(ipField);
      expect(ipField).toBeChecked();

      waitFor(async () => {
        const ipsField = screen.getByRole("textbox", {
          name: "IP addresses *",
        });

        await user.type(ipsField, "invalid");
        expect(ipsField).toBeInvalid();

        await user.type(ipsField, "111.111.11.11");
        expect(ipsField).toBeValid();
      });
    });
  });

  describe("Form isValid state", () => {
    afterEach(() => {
      cleanup();
    });

    it("renders an invalid form when required fields have missing or invalid values", async () => {
      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "1",
            topictype: "Consumer",
            aclPatternType: "LITERAL",
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
        { queryClient: true }
      );

      const submitButton = screen.getByRole("button", { name: "Submit" });
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });

      await userEvent.click(principalField);

      expect(submitButton).not.toBeEnabled();
    });

    it("renders form when all required fields have valid values", async () => {
      const { result } = renderHook(() =>
        useForm<TopicConsumerFormSchema>({
          schema: topicConsumerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "1",
            topictype: "Consumer",
            aclPatternType: "LITERAL",
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
        { queryClient: true }
      );

      const submitButton = screen.getByRole("button", { name: "Submit" });
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });

      await userEvent.click(principalField);

      waitFor(async () => {
        const principalsField = screen.getByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(submitButton).not.toBeEnabled();

        await userEvent.type(principalsField, "Alice");

        expect(submitButton).toBeEnabled();
      });
    });
  });
});

// Environment
// - render all env passed as props in options

// Ip or principal based
// - Render SSL string when Principal is checked
// - Render IP field when IP is checked
// - Is error when incorrect IP is entered
// - Is error when nothing is entered and blur

// Form
// Render all fields, with the required ones marked as required
// Invalid when a reauired field is missing
// Valid when all fields are
