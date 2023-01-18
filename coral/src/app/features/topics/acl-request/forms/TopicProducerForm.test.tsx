import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, renderHook, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useForm } from "src/app/components/Form";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";
import TopicProducerForm, {
  TopicProducerFormProps,
} from "src/app/features/topics/acl-request/forms/TopicProducerForm";
import topicProducerFormSchema, {
  TopicProducerFormSchema,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-producer";
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
} as TopicProducerFormProps;

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
} as TopicProducerFormProps;

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
} as TopicProducerFormProps;

describe("<TopicAclRequest />", () => {
  describe("renders correct fields in pristine form", () => {
    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "placeholder",
            topictype: "Producer",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicProducerForm
            {...baseProps}
            topicProducerForm={result.current}
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
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "1",
            topictype: "Producer",
            aclIpPrincipleType: "PRINCIPAL",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicProducerForm
            {...basePropsIsAivenCluster}
            topicProducerForm={result.current}
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
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "2",
            topictype: "Producer",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicProducerForm
            {...basePropsNotAivenCluster}
            topicProducerForm={result.current}
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
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "2",
            topictype: "Producer",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicProducerForm
            {...basePropsNotAivenCluster}
            topicProducerForm={result.current}
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

  describe("aclPatternType fields (LITERAL or PREFIX) and TopicNameOrPrefixField", () => {
    let user: ReturnType<typeof userEvent.setup>;

    beforeEach(() => {
      user = userEvent.setup();
    });

    beforeAll(() => {
      const { result } = renderHook(() =>
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "placeholder",
            topictype: "Producer",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicProducerForm
            {...baseProps}
            topicProducerForm={result.current}
          />
        </AquariumContext>,
        { queryClient: true }
      );
    });

    afterAll(() => {
      cleanup();
    });

    it("renders correct fields when selecting Literal or Prefixed in aclPatternType fields", async () => {
      const literalField = screen.getByRole("radio", { name: "Literal" });
      const prefixedField = screen.getByRole("radio", {
        name: "Prefixed",
      });
      const topicNameField = screen.queryByRole("combobox", {
        name: "Topic name *",
      });
      const prefixField = screen.queryByRole("textbox", {
        name: "Prefix",
      });

      expect(literalField).toBeVisible();
      expect(literalField).toBeEnabled();
      expect(literalField).not.toBeChecked();
      expect(prefixedField).toBeVisible();
      expect(prefixedField).toBeEnabled();
      expect(prefixedField).not.toBeChecked();
      expect(topicNameField).toBeNull();
      expect(prefixField).toBeNull();

      await user.click(literalField);
      expect(literalField).toBeChecked();
      waitFor(() => {
        expect(prefixField).toBeNull();
        expect(topicNameField).toBeInTheDocument();
        expect(topicNameField).toBeEnabled();
        expect(topicNameField).toHaveDisplayValue("aivtopic1");
      });

      await user.click(prefixedField);
      expect(prefixedField).toBeChecked();
      waitFor(() => {
        expect(topicNameField).toBeNull();
        expect(prefixField).toBeInTheDocument();
        expect(prefixField).toBeEnabled();
        expect(prefixField).toHaveDisplayValue("aivtopic1");
      });
    });
  });

  describe("Form isValid state", () => {
    afterEach(() => {
      cleanup();
    });

    it("renders an invalid form when required fields have missing or invalid values", async () => {
      const { result } = renderHook(() =>
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "1",
            topictype: "Producer",
            aclPatternType: "LITERAL",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicProducerForm
            {...basePropsNotAivenCluster}
            topicProducerForm={result.current}
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
        useForm<TopicProducerFormSchema>({
          schema: topicProducerFormSchema,
          defaultValues: {
            topicname: "aiventopic1",
            environment: "1",
            topictype: "Producer",
            aclPatternType: "LITERAL",
          },
        })
      );
      customRender(
        <AquariumContext>
          <TopicProducerForm
            {...basePropsNotAivenCluster}
            topicProducerForm={result.current}
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
