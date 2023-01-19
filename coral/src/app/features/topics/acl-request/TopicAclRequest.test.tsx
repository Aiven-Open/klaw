import { cleanup, screen, waitFor } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { Route, Routes } from "react-router-dom";
import TopicAclRequest from "src/app/features/topics/acl-request/TopicAclRequest";
import {
  getMockedResponseGetClusterInfoFromEnv,
  mockGetClusterInfoFromEnv,
  mockGetEnvironments,
} from "src/domain/environment/environment-api.msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import {
  mockedResponseTopicNames,
  mockedResponseTopicTeamLiteral,
  mockGetTopicNames,
  mockGetTopicTeam,
} from "src/domain/topic/topic-api.msw";
import { server } from "src/services/api-mocks/server";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const dataSetup = ({ isAivenCluster }: { isAivenCluster: boolean }) => {
  mockGetEnvironments({
    mswInstance: server,
    response: {
      data: [
        createMockEnvironmentDTO({
          name: "TST",
          id: "1",
          maxPartitions: "6",
          maxReplicationFactor: "2",
          defaultPartitions: "3",
          defaultReplicationFactor: "2",
        }),
        createMockEnvironmentDTO({
          name: "DEV",
          id: "2",
          maxPartitions: undefined,
          maxReplicationFactor: undefined,
          defaultPartitions: "2",
          defaultReplicationFactor: "2",
        }),
        createMockEnvironmentDTO({
          name: "PROD",
          id: "3",
          maxPartitions: "16",
          maxReplicationFactor: "3",
          defaultPartitions: "2",
          defaultReplicationFactor: "2",
        }),
      ],
    },
  });
  mockGetTopicNames({
    mswInstance: server,
    response: mockedResponseTopicNames,
  });
  mockGetTopicTeam({
    mswInstance: server,
    response: mockedResponseTopicTeamLiteral,
    topicName: "aivtopic1",
  });
  mockGetClusterInfoFromEnv({
    mswInstance: server,
    response: getMockedResponseGetClusterInfoFromEnv(isAivenCluster),
  });
};

const assertSkeleton = async () => {
  const skeleton = screen.getByTestId("skeleton");
  expect(skeleton).toBeVisible();
  await waitForElementToBeRemoved(skeleton);
};

describe("<TopicAclRequest />", () => {
  let user: ReturnType<typeof userEvent.setup>;

  // Data mocking
  beforeAll(async () => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe("Form states (producer, consumer)", () => {
    //Render stuff
    beforeEach(() => {
      dataSetup({ isAivenCluster: true });

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/acl/request"
            element={<TopicAclRequest />}
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: "/topic/aivtopic1/acl/request",
        }
      );
    });

    // Clear stuff
    afterEach(cleanup);

    it("renders TopicProducerForm by by default", async () => {
      await assertSkeleton();

      const aclProducerTypeInput = screen.getByRole("radio", {
        name: "Producer",
      });
      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      // Only rendered in Producer form
      const transactionalIdInput = screen.getByLabelText("Transactional ID");

      expect(aclProducerTypeInput).toBeVisible();
      expect(aclProducerTypeInput).toBeChecked();
      expect(aclConsumerTypeInput).not.toBeChecked();
      expect(transactionalIdInput).toBeVisible();
    });

    it("renders the correct AclIpPrincipleTypeField with Principal option checked when choosing an Aiven cluster environment", async () => {
      await assertSkeleton();

      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });
      const ipField = screen.getByRole("radio", {
        name: "IP",
      });

      expect(principalField).not.toBeEnabled();
      expect(principalField).not.toBeChecked();
      expect(ipField).not.toBeEnabled();
      expect(ipField).not.toBeChecked();

      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
      });
      const option = screen.getByRole("option", { name: "TST" });
      await userEvent.selectOptions(environmentField, option);

      await waitFor(() => {
        expect(principalField).toBeEnabled();
        expect(principalField).toBeChecked();
        expect(ipField).toBeDisabled();
        expect(ipField).not.toBeChecked();
      });

      const principalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      await waitFor(() => {
        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();
      });
    });

    it("renders the appropriate form when switching between Producer and Consumer ACL types", async () => {
      await assertSkeleton();

      const aclProducerTypeInput = screen.getByRole("radio", {
        name: "Producer",
      });
      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });

      expect(aclConsumerTypeInput).toBeVisible();
      expect(aclConsumerTypeInput).not.toBeChecked();

      await user.click(aclConsumerTypeInput);

      // Only rendered in Consumer form
      const consumerGroupInput = screen.getByLabelText("Consumer group*");

      expect(aclConsumerTypeInput).toBeChecked();
      expect(aclProducerTypeInput).not.toBeChecked();
      expect(consumerGroupInput).toBeVisible();
    });
  });

  describe("User interaction (TopicProducerForm)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: false });

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/acl/request"
            element={<TopicAclRequest />}
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: "/topic/aivtopic1/acl/request",
        }
      );
    });

    afterEach(cleanup);

    it("renders correct fields when selecting IP or Principal in AclIpPrincipleTypeField", async () => {
      await assertSkeleton();

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
      expect(ipField).not.toBeEnabled();
      expect(ipField).not.toBeChecked();
      expect(principalField).toBeVisible();
      expect(principalField).not.toBeEnabled();
      expect(principalField).not.toBeChecked();
      expect(hiddenIpsField).toBeNull();
      expect(hiddenPrincipalsField).toBeNull();

      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
      });
      const option = screen.getByRole("option", { name: "TST" });
      await userEvent.selectOptions(environmentField, option);

      await user.click(principalField);

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await user.click(ipField);

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });
      expect(visibleIpsField).toBeInTheDocument();
      expect(visibleIpsField).toBeEnabled();
    });

    it("error when entering invalid IP in IPs field", async () => {
      await assertSkeleton();

      const ipField = screen.getByRole("radio", { name: "IP" });

      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
      });
      const option = screen.getByRole("option", { name: "TST" });
      await userEvent.selectOptions(environmentField, option);

      await user.click(ipField);

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await user.type(visibleIpsField, "invalid{Enter}");
      await waitFor(() => expect(visibleIpsField).toBeInvalid());
    });

    it("does not error when entering valid IP in IPs field", async () => {
      await assertSkeleton();

      const ipField = screen.getByRole("radio", { name: "IP" });

      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
      });
      const option = screen.getByRole("option", { name: "TST" });
      await userEvent.selectOptions(environmentField, option);

      await user.click(ipField);

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await user.type(visibleIpsField, "111.111.11.11{Enter}");
      expect(visibleIpsField).toBeValid();
    });

    it("renders correct fields when selecting Literal or Prefixed in aclPatternType fields", async () => {
      await assertSkeleton();

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

      const visibleTopicNameField = await screen.findByRole("combobox", {
        name: "Topic name *",
      });

      expect(hiddenPrefixField).toBeNull();
      expect(visibleTopicNameField).toBeInTheDocument();
      expect(visibleTopicNameField).toBeEnabled();
      expect(visibleTopicNameField).toHaveDisplayValue("aivtopic1");

      await user.click(prefixedField);
      expect(prefixedField).toBeChecked();

      const visiblePrefixField = await screen.findByRole("textbox", {
        name: "Prefix *",
      });

      expect(hiddenTopicNameField).toBeNull();
      expect(visiblePrefixField).toBeInTheDocument();
      expect(visiblePrefixField).toBeEnabled();
      expect(visiblePrefixField).toHaveDisplayValue("aivtopic1");
    });
  });

  describe("User interaction (TopicConsumerForm)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: false });

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/acl/request"
            element={<TopicAclRequest />}
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          customRoutePath: "/topic/aivtopic1/acl/request",
        }
      );
    });

    afterEach(cleanup);

    it("renders correct fields when selecting IP or Principal in AclIpPrincipleTypeField", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await user.click(aclConsumerTypeInput);

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
      expect(ipField).not.toBeEnabled();
      expect(ipField).not.toBeChecked();
      expect(principalField).toBeVisible();
      expect(principalField).not.toBeEnabled();
      expect(principalField).not.toBeChecked();
      expect(hiddenIpsField).toBeNull();
      expect(hiddenPrincipalsField).toBeNull();

      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
      });
      const option = screen.getByRole("option", { name: "TST" });
      await userEvent.selectOptions(environmentField, option);

      await user.click(principalField);

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await user.click(ipField);

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });
      expect(visibleIpsField).toBeInTheDocument();
      expect(visibleIpsField).toBeEnabled();
    });

    it("error when entering invalid IP in IPs field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await user.click(aclConsumerTypeInput);

      const ipField = screen.getByRole("radio", { name: "IP" });

      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
      });
      const option = screen.getByRole("option", { name: "TST" });
      await userEvent.selectOptions(environmentField, option);

      await user.click(ipField);

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await user.type(visibleIpsField, "invalid{Enter}");
      await waitFor(() => expect(visibleIpsField).toBeInvalid());
    });

    it("does not error when entering valid IP in IPs field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await user.click(aclConsumerTypeInput);

      const ipField = screen.getByRole("radio", { name: "IP" });

      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
      });
      const option = screen.getByRole("option", { name: "TST" });
      await userEvent.selectOptions(environmentField, option);

      await user.click(ipField);

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await user.type(visibleIpsField, "111.111.11.11{Enter}");
      await waitFor(() => expect(visibleIpsField).toBeValid());
    });
  });
});
