import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen, waitFor } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { Route, Routes } from "react-router-dom";
import TopicAclRequest from "src/app/features/topics/acl-request/TopicAclRequest";
import {
  mockedResponseGetClusterInfoFromEnv,
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

describe("<TopicAclRequest />", () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeAll(() => {
    server.listen();

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
      response: mockedResponseGetClusterInfoFromEnv,
    });
    customRender(
      <AquariumContext>
        <Routes>
          <Route
            path="/topic/:topicName/acl/request"
            element={<TopicAclRequest />}
          />
        </Routes>
      </AquariumContext>,
      {
        queryClient: true,
        memoryRouter: true,
        customRoutePath: "/topic/aivtopic1/acl/request",
      }
    );
  });

  afterAll(() => {
    cleanup();
    server.close();
  });

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe("<TopicAclRequest>", () => {
    beforeAll(async () => {
      const skeleton = screen.getByTestId("skeleton");

      expect(skeleton).toBeVisible();
      await waitForElementToBeRemoved(skeleton);
    });

    it("renders TopicProducerForm by by default", async () => {
      const aclProducerTypeInput = screen.getByLabelText("Producer");
      const aclConsumerTypeInput = screen.getByLabelText("Consumer");
      // Only rendered in Producer form
      const transactionalIdInput = screen.getByLabelText("Transactional ID");

      expect(aclProducerTypeInput).toBeVisible();
      expect(aclProducerTypeInput).toBeChecked();
      expect(aclConsumerTypeInput).not.toBeChecked();
      expect(transactionalIdInput).toBeVisible();
    });

    it("renders the appropriate form when switching between Producer and Consumer ACL types", async () => {
      const aclProducerTypeInput = screen.getByLabelText("Producer");
      const aclConsumerTypeInput = screen.getByLabelText("Consumer");

      expect(aclConsumerTypeInput).toBeVisible();
      expect(aclConsumerTypeInput).not.toBeChecked();

      await user.click(aclConsumerTypeInput);

      // Only rendered in Consumer form
      const consumerGroupInput = screen.getByLabelText("Consumer group*");

      expect(aclConsumerTypeInput).toBeChecked();
      expect(aclProducerTypeInput).not.toBeChecked();
      expect(consumerGroupInput).toBeVisible();
    });

    it("renders the correct AclIpPrincipleTypeField with Principal option checked when choosing an Aiven cluster environment", async () => {
      const principalField = screen.getByRole("radio", {
        name: "Principal",
      });
      const ipField = screen.getByRole("radio", {
        name: "IP",
      });
      const environmentField = screen.getByRole("combobox", {
        name: "Select environment *",
      });
      const option = screen.getByRole("option", { name: "TST" });

      await userEvent.selectOptions(environmentField, option);

      waitFor(() => {
        expect(principalField).toBeEnabled();
        expect(principalField).toBeChecked();
        expect(ipField).toBeDisabled();
        expect(ipField).not.toBeChecked();

        const principalsField = screen.getByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();
      });
    });
  });
});
