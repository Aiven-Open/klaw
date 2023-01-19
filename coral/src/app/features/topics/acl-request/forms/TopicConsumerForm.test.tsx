import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, renderHook, screen } from "@testing-library/react";
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

describe("<TopicConsumerForm />", () => {
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
});
