import { cleanup, renderHook, screen } from "@testing-library/react";
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

describe("<TopicProducerForm />", () => {
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
        <TopicProducerForm {...baseProps} topicProducerForm={result.current} />,
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
        <TopicProducerForm
          {...basePropsIsAivenCluster}
          topicProducerForm={result.current}
        />,
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
      const hiddenIpsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });
      const hiddenPrincipalsField = screen.getByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(hiddenIpsField).toBeNull();
      expect(hiddenPrincipalsField).toBeVisible();
      expect(hiddenPrincipalsField).toBeEnabled();
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
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />,
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

    afterEach(() => {
      cleanup();
    });

    it("renders correct fields when selecting IP or Principal in AclIpPrincipleTypeField", async () => {
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

      const { rerender } = customRender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />,
        { queryClient: true }
      );

      const ipField = screen.getByRole("radio", { name: "IP" });
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });
      const hiddenIpsField = screen.queryByRole("textbox", {
        name: "IP addresses *",
      });
      const hiddenPrincipalsField = screen.queryByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(ipField).toBeVisible();
      expect(ipField).toBeEnabled();
      expect(ipField).not.toBeChecked();
      expect(principalField).toBeVisible();
      expect(principalField).toBeEnabled();
      expect(principalField).not.toBeChecked();
      expect(hiddenIpsField).toBeNull();
      expect(hiddenPrincipalsField).toBeNull();

      await user.click(principalField);
      expect(principalField).toBeChecked();

      rerender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />
      );

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await user.click(ipField);
      expect(ipField).toBeChecked();

      rerender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />
      );

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });
      expect(visibleIpsField).toBeInTheDocument();
      expect(visibleIpsField).toBeEnabled();
    });

    it("error when entering invalid IP in IPs field", async () => {
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
      const { rerender } = customRender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />,
        { queryClient: true }
      );

      const ipField = screen.getByRole("radio", { name: "IP" });

      await user.click(ipField);
      expect(ipField).toBeChecked();

      rerender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />
      );

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await user.type(visibleIpsField, "invalid{Enter}");
      rerender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />
      );
      expect(visibleIpsField).toBeInvalid();
    });

    it("does not error when entering valid IP", async () => {
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
      const { rerender } = customRender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />,
        { queryClient: true }
      );

      const ipField = screen.getByRole("radio", { name: "IP" });

      await user.click(ipField);
      expect(ipField).toBeChecked();

      rerender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />
      );

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await user.clear(visibleIpsField);
      await user.type(visibleIpsField, "111.111.11.11{Enter}");

      rerender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />
      );
      expect(visibleIpsField).toBeValid();
    });
  });

  describe("aclPatternType fields (LITERAL or PREFIX) and TopicNameOrPrefixField", () => {
    let user: ReturnType<typeof userEvent.setup>;

    beforeEach(() => {
      user = userEvent.setup();
    });

    afterAll(() => {
      cleanup();
    });

    it("renders correct fields when selecting Literal or Prefixed in aclPatternType fields", async () => {
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
      const { rerender } = customRender(
        <TopicProducerForm {...baseProps} topicProducerForm={result.current} />,
        { queryClient: true }
      );

      const literalField = screen.getByRole("radio", { name: "Literal" });
      const prefixedField = screen.getByRole("radio", {
        name: "Prefixed",
      });
      const hiddenTopicNameField = screen.queryByRole("combobox", {
        name: "Topic name *",
      });
      const hiddenPrefixField = screen.queryByRole("textbox", {
        name: "Prefix *",
      });

      expect(literalField).toBeVisible();
      expect(literalField).toBeEnabled();
      expect(literalField).not.toBeChecked();
      expect(prefixedField).toBeVisible();
      expect(prefixedField).toBeEnabled();
      expect(prefixedField).not.toBeChecked();
      expect(hiddenTopicNameField).toBeNull();
      expect(hiddenPrefixField).toBeNull();

      await user.click(literalField);
      expect(literalField).toBeChecked();

      rerender(
        <TopicProducerForm {...baseProps} topicProducerForm={result.current} />
      );

      const visibleTopicNameField = await screen.findByRole("combobox", {
        name: "Topic name *",
      });

      expect(hiddenPrefixField).toBeNull();
      expect(visibleTopicNameField).toBeInTheDocument();
      expect(visibleTopicNameField).toBeEnabled();
      expect(visibleTopicNameField).toHaveDisplayValue("aiventopic1");

      await user.click(prefixedField);
      expect(prefixedField).toBeChecked();

      rerender(
        <TopicProducerForm {...baseProps} topicProducerForm={result.current} />
      );

      const visiblePrefixField = await screen.findByRole("textbox", {
        name: "Prefix *",
      });

      expect(hiddenTopicNameField).toBeNull();
      expect(visiblePrefixField).toBeInTheDocument();
      expect(visiblePrefixField).toBeEnabled();
      expect(visiblePrefixField).toHaveDisplayValue("aiventopic1");
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
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />,
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

      const { rerender } = customRender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />,
        { queryClient: true }
      );

      const submitButton = screen.getByRole("button", { name: "Submit" });
      const remarksField = screen.getByRole("textbox", { name: "Remarks" });
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });

      await userEvent.click(principalField);

      rerender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />
      );

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      await userEvent.type(visiblePrincipalsField, "Alice{Enter}");

      await userEvent.tab();
      expect(visiblePrincipalsField).toBeValid();

      rerender(
        <TopicProducerForm
          {...basePropsNotAivenCluster}
          topicProducerForm={result.current}
        />
      );

      // await waitFor(() => {
      screen.debug(screen.getByRole("button", { name: "Submit" }), 100000);
      expect(submitButton).toBeEnabled();
      // });
    });
  });
});
