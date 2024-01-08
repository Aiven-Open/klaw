import { cleanup, screen } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { userEvent } from "@testing-library/user-event";
import { Route, Routes } from "react-router-dom";
import TopicAclRequest from "src/app/features/topics/acl-request/TopicAclRequest";
import { getClusterInfoFromEnvironment } from "src/domain/cluster";
import { getMockedResponseGetClusterInfoFromEnvironment } from "src/domain/cluster/cluster-api-test-helper";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { getTopicNames, getTopicTeam } from "src/domain/topic";
import {
  mockedResponseTopicNames,
  mockedResponseTopicTeamLiteral,
} from "src/domain/topic/topic-test-helper";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

jest.mock("src/domain/acl/acl-api", () => ({
  ...jest.requireActual("src/domain/acl/acl-api"),
  getAivenServiceAccounts: jest.fn().mockReturnValue(["account"]),
}));

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

jest.mock("src/domain/environment/environment-api.ts");
const mockgetAllEnvironmentsForTopicAndAcl =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;

jest.mock("src/domain/topic/topic-api.ts");
const mockGetTopicNames = getTopicNames as jest.MockedFunction<
  typeof getTopicNames
>;
const mockGetTopicTeam = getTopicTeam as jest.MockedFunction<
  typeof getTopicTeam
>;

jest.mock("src/domain/cluster/cluster-api.ts");
const mockGetClusterInfoFromEnv =
  getClusterInfoFromEnvironment as jest.MockedFunction<
    typeof getClusterInfoFromEnvironment
  >;

const dataSetup = ({ isAivenCluster }: { isAivenCluster: boolean }) => {
  mockgetAllEnvironmentsForTopicAndAcl.mockResolvedValue([
    createMockEnvironmentDTO({
      name: "TST",
      id: "1",
    }),
    createMockEnvironmentDTO({
      name: "DEV",
      id: "2",
    }),
    createMockEnvironmentDTO({
      name: "PROD",
      id: "3",
    }),
  ]);
  mockGetTopicNames.mockResolvedValue(mockedResponseTopicNames);
  mockGetTopicTeam.mockResolvedValue(mockedResponseTopicTeamLiteral);
  mockGetClusterInfoFromEnv.mockResolvedValue(
    getMockedResponseGetClusterInfoFromEnvironment(isAivenCluster)
  );
};

const assertSkeleton = async () => {
  const skeleton = screen.getByTestId("skeleton");
  expect(skeleton).toBeVisible();
  await waitForElementToBeRemoved(skeleton);
};

const selectTestEnvironment = async () => {
  const environmentField = screen.getByRole("combobox", {
    name: /Environment/,
  });
  const option = screen.getByRole("option", { name: "TST" });
  await userEvent.selectOptions(environmentField, option);
};

describe("<TopicAclRequest />", () => {
  describe("/topic/:topicName/subscribe: Form states (producer, consumer)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: true });

      customRender(
        <Routes>
          <Route
            path="/topic/:topicName/subscribe"
            element={<TopicAclRequest />}
          />
        </Routes>,
        {
          queryClient: true,
          memoryRouter: true,
          aquariumContext: true,
          customRoutePath: "/topic/aivtopic1/subscribe?env=1",
        }
      );
    });

    afterEach(cleanup);

    it("renders TopicProducerForm by default", async () => {
      await assertSkeleton();

      const aclProducerTypeInput = screen.getByRole("radio", {
        name: "Producer",
      });
      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });

      expect(aclProducerTypeInput).toBeVisible();
      expect(aclProducerTypeInput).toBeChecked();
      expect(aclConsumerTypeInput).not.toBeChecked();
    });

    it("renders the correct AclIpPrincipleTypeField when rendering with an Aiven cluster environment", async () => {
      await assertSkeleton();

      const principalField = screen.getByRole("radio", {
        name: "Service account",
      });
      const ipField = screen.getByRole("radio", {
        name: "IP",
      });

      expect(principalField).not.toBeEnabled();
      expect(principalField).toBeChecked();
      expect(ipField).not.toBeEnabled();
      expect(ipField).not.toBeChecked();
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

      await userEvent.click(aclConsumerTypeInput);

      // Only rendered in Producer form
      const transactionalIdInput = screen.queryByLabelText("Transactional ID");

      expect(aclConsumerTypeInput).toBeChecked();
      expect(aclProducerTypeInput).not.toBeChecked();
      expect(transactionalIdInput).toBeNull();
    });
  });

  describe("/request/acl: Form states (producer, consumer)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: true });

      customRender(<TopicAclRequest />, {
        queryClient: true,
        memoryRouter: true,
        aquariumContext: true,
      });
    });

    afterEach(cleanup);

    it("renders TopicProducerForm by default", async () => {
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

      await selectTestEnvironment();

      expect(principalField).not.toBeEnabled();
      expect(principalField).toBeChecked();
      expect(ipField).toBeDisabled();
      expect(ipField).not.toBeChecked();

      const principalsField = await screen.findByRole("combobox", {
        name: "Service accounts *",
      });

      expect(principalsField).toBeVisible();
      expect(principalsField).toBeEnabled();
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

      await userEvent.click(aclConsumerTypeInput);

      // Only rendered in Producer form
      const transactionalIdInput = screen.queryByLabelText("Transactional ID");

      expect(aclConsumerTypeInput).toBeChecked();
      expect(aclProducerTypeInput).not.toBeChecked();
      expect(transactionalIdInput).toBeNull();
    });
  });
});
