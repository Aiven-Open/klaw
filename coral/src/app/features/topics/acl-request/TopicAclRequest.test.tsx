import { cleanup, screen, waitFor, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { Route, Routes } from "react-router-dom";
import TopicAclRequest from "src/app/features/topics/acl-request/TopicAclRequest";
import { mockCreateAclRequest } from "src/domain/acl/acl-api-msw";
import {
  getMockedResponseGetClusterInfoFromEnv,
  mockGetClusterInfoFromEnv,
  mockgetAllEnvironmentsForTopicAndAcl,
} from "src/domain/environment/environment-api.msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import {
  mockGetTopicNames,
  mockGetTopicTeam,
  mockedResponseTopicNames,
  mockedResponseTopicTeamLiteral,
} from "src/domain/topic/topic-api.msw";
import api from "src/services/api";
import { server } from "src/services/api-mocks/server";
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

const dataSetup = ({ isAivenCluster }: { isAivenCluster: boolean }) => {
  mockgetAllEnvironmentsForTopicAndAcl({
    mswInstance: server,
    response: {
      data: [
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

const selectTestEnvironment = async () => {
  const environmentField = screen.getByRole("combobox", {
    name: "Environment *",
  });
  const option = screen.getByRole("option", { name: "TST" });
  await userEvent.selectOptions(environmentField, option);
};

describe("<TopicAclRequest />", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

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
      expect(principalField).not.toBeChecked();
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

  describe("/topic/:topicName/subscribe: User interaction (TopicProducerForm)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: false });

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
          customRoutePath: "/topic/aivtopic1/subscribe?env=1",
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

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
      expect(ipField).toBeEnabled();
      expect(ipField).not.toBeChecked();
      expect(principalField).toBeVisible();
      expect(principalField).toBeEnabled();
      expect(principalField).not.toBeChecked();
      expect(hiddenIpsField).toBeNull();
      expect(hiddenPrincipalsField).toBeNull();

      await userEvent.click(principalField);

      expect(principalField).toBeChecked();

      const principalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(principalsField).toBeInTheDocument();
      expect(principalsField).toBeEnabled();

      await userEvent.click(ipField);

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      expect(visibleIpsField).toBeInTheDocument();
      expect(visibleIpsField).toBeEnabled();
    });

    it("error when entering invalid IP in IPs field", async () => {
      await assertSkeleton();

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const ipsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(ipsField, "invalid{Enter}");
      await waitFor(() => expect(ipsField).toBeInvalid());
      await waitFor(() =>
        expect(screen.getByText("Invalid IP address.")).toBeVisible()
      );
    });

    it("does not error when entering valid IP in IPs field", async () => {
      await assertSkeleton();

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "111.111.11.11{Enter}");
      expect(visibleIpsField).toBeValid();
    });

    it("errors when entering more than 15 IPs in IPs field", async () => {
      await assertSkeleton();

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "111.111.11.11{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.12{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.13{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.14{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.15{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.16{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.17{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.18{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.19{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.20{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.21{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.22{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.23{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.24{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.25{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.26{Enter}");

      await waitFor(() => expect(visibleIpsField).not.toBeValid());
      await waitFor(() =>
        expect(screen.getByText("Maximum 15 elements allowed.")).toBeVisible()
      );
    });

    it("errors when entering more than 5 elements in Principal field", async () => {
      await assertSkeleton();

      const principalField = screen.getByRole("radio", { name: "Principal" });

      await selectTestEnvironment();

      expect(principalField).toBeEnabled();

      await userEvent.click(principalField);

      expect(principalField).toBeChecked();

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await userEvent.type(visiblePrincipalsField, "Alice1{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice2{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice3{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice4{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice5{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice6{Enter}");

      await waitFor(() => expect(visiblePrincipalsField).not.toBeValid());
      await waitFor(() =>
        expect(screen.getByText("Maximum 5 elements allowed.")).toBeVisible()
      );
    });

    it("errors when entering a wrong value in Transactional ID field", async () => {
      await assertSkeleton();

      const transactionalIdInput = screen.getByLabelText("Transactional ID");

      await userEvent.type(transactionalIdInput, "Hello invalid");
      await userEvent.tab();

      await waitFor(() => expect(transactionalIdInput).not.toBeValid());
      await waitFor(() =>
        expect(
          screen.getByText("Only characters allowed: a-z, A-Z, 0-9, ., _,-.")
        ).toBeVisible()
      );
    });

    it("errors when entering a too long value in Transactional ID field", async () => {
      await assertSkeleton();

      const transactionalIdInput = screen.getByLabelText("Transactional ID");
      const tooLong = new Array(152).join("a");
      await userEvent.type(transactionalIdInput, tooLong);
      await userEvent.tab();

      await waitFor(() => expect(transactionalIdInput).not.toBeValid());
      await waitFor(() =>
        expect(
          screen.getByText(
            "Transactional ID cannot be more than 150 characters."
          )
        ).toBeVisible()
      );
    });

    it("does not error when entering a correct value in Transactional ID field", async () => {
      await assertSkeleton();

      const transactionalIdInput = screen.getByLabelText("Transactional ID");

      await userEvent.type(transactionalIdInput, "HelloValid");
      await userEvent.tab();

      await waitFor(() => expect(transactionalIdInput).toBeValid());
    });

    it("renders an enabled Submit button when there is an error", async () => {
      await assertSkeleton();

      const transactionalIdInput = screen.getByLabelText("Transactional ID");
      const tooLong = new Array(152).join("a");
      await userEvent.type(transactionalIdInput, tooLong);
      await userEvent.tab();

      expect(
        screen.getByRole("button", { name: "Submit request" })
      ).toBeEnabled();
    });

    it("navigates back when clicking Cancel (TopicProducerForm)", async () => {
      await assertSkeleton();

      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      mockedNavigate.mockClear();
      await userEvent.click(cancelButton);

      await waitFor(() => {
        expect(mockedNavigate).toHaveBeenCalledTimes(1);
        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });
    });

    it("navigates back when clicking Cancel (TopicConsumerForm)", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });

      await userEvent.click(aclConsumerTypeInput);

      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      mockedNavigate.mockClear();
      await userEvent.click(cancelButton);

      await waitFor(() => {
        expect(mockedNavigate).toHaveBeenCalledTimes(1);
        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });
    });
  });

  describe("/topic/:topicName/subscribe: User interaction (TopicConsumerForm, NOT Aiven cluster)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: false });

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
          customRoutePath: "/topic/aivtopic1/subscribe?env=1",
        }
      );
    });

    afterEach(cleanup);

    it("renders correct fields when selecting IP or Principal in AclIpPrincipleTypeField", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

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

      await userEvent.click(principalField);

      expect(principalField).toBeChecked();

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await userEvent.click(ipField);

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
      await userEvent.click(aclConsumerTypeInput);

      const ipField = screen.getByRole("radio", { name: "IP" });

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "invalid{Enter}");
      await waitFor(() => expect(visibleIpsField).toBeInvalid());
    });

    it("does not error when entering valid IP in IPs field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const ipField = screen.getByRole("radio", { name: "IP" });

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "111.111.11.11{Enter}");

      await waitFor(() => expect(visibleIpsField).toBeValid());
    });

    it("errors when entering more than 15 IPs in IPs field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const ipField = screen.getByRole("radio", { name: "IP" });

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "111.111.11.11{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.12{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.13{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.14{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.15{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.16{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.17{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.18{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.19{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.20{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.21{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.22{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.23{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.24{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.25{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.26{Enter}");

      await waitFor(() => expect(visibleIpsField).not.toBeValid());
      await waitFor(() =>
        expect(screen.getByText("Maximum 15 elements allowed.")).toBeVisible()
      );
    });

    it("errors when entering more than 5 elements in Principal field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const principalField = screen.getByRole("radio", { name: "Principal" });

      expect(principalField).toBeEnabled();

      await userEvent.click(principalField);

      expect(principalField).toBeChecked();

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await userEvent.type(visiblePrincipalsField, "Alice1{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice2{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice3{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice4{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice5{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice6{Enter}");

      await waitFor(() => expect(visiblePrincipalsField).not.toBeValid());
      await waitFor(() =>
        expect(screen.getByText("Maximum 5 elements allowed.")).toBeVisible()
      );
    });

    it("errors when entering a wrong value in Consumer Group field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const consumerGroupInput = await screen.findByRole("textbox", {
        name: "Consumer group *",
      });

      await userEvent.type(consumerGroupInput, "Hello invalid");
      await userEvent.tab();

      await waitFor(() => expect(consumerGroupInput).not.toBeValid());
      await waitFor(() =>
        expect(
          screen.getByText("Only characters allowed: a-z, A-Z, 0-9, ., _,-.")
        ).toBeVisible()
      );
    });

    it("errors when entering a too long value in Consumer group field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const consumerGroupInput = await screen.findByRole("textbox", {
        name: "Consumer group *",
      });
      const tooLong = new Array(152).join("a");
      await userEvent.type(consumerGroupInput, tooLong);
      await userEvent.tab();

      await waitFor(() => expect(consumerGroupInput).not.toBeValid());
      await waitFor(() =>
        expect(
          screen.getByText("Consumer group cannot be more than 150 characters.")
        ).toBeVisible()
      );
    });

    it("does not error when entering a correct value in Consumer group field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const consumerGroupInput = await screen.findByRole("textbox", {
        name: "Consumer group *",
      });

      await userEvent.type(consumerGroupInput, "HelloValid");
      await userEvent.tab();

      await waitFor(() => expect(consumerGroupInput).toBeValid());
    });
  });

  describe("/topic/:topicName/subscribe: Form submission (TopicProducerForm)", () => {
    const locationAssignSpy = jest.fn();
    let originalLocation: Location;

    beforeAll(() => {
      originalLocation = window.location;
      Object.defineProperty(global.window, "location", {
        writable: true,
        value: {
          assign: locationAssignSpy,
        },
      });
    });

    afterAll(() => {
      global.window.location = originalLocation;
    });

    beforeEach(async () => {
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
          customRoutePath: "/topic/aivtopic1/subscribe?env=1",
        }
      );
    });

    afterEach(cleanup);

    describe("when user cancels form input", () => {
      beforeEach(async () => {
        await assertSkeleton();
        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Producer",
        });
        await userEvent.click(aclConsumerTypeInput);
      });

      const getForm = () => {
        return screen.getByRole("form", {
          name: `Request producer ACL`,
        });
      };

      it("redirects user to the previous page if they click 'Cancel' on empty form", async () => {
        const form = getForm();

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);

        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });

      it('shows a warning dialog if user clicks "Cancel" and has inputs in form', async () => {
        mockedNavigate.mockClear();
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);
        const dialog = screen.getByRole("dialog");

        expect(dialog).toBeVisible();
        expect(dialog).toHaveTextContent("Cancel ACL request?");
        expect(dialog).toHaveTextContent(
          "Do you want to cancel this request? The data added will be lost."
        );

        expect(mockedNavigate).not.toHaveBeenCalled();
      });

      it("brings the user back to the form when they do not cancel", async () => {
        mockedNavigate.mockClear();
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);
        const dialog = screen.getByRole("dialog");

        const returnButton = screen.getByRole("button", {
          name: "Continue with request",
        });

        await userEvent.click(returnButton);

        expect(mockedNavigate).not.toHaveBeenCalled();

        expect(dialog).not.toBeInTheDocument();
      });

      it("redirects user to previous page if they cancel the request", async () => {
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);

        const returnButton = screen.getByRole("button", {
          name: "Cancel request",
        });

        await userEvent.click(returnButton);

        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });
    });

    describe("when API returns an error", () => {
      const originalConsoleError = console.error;
      beforeEach(async () => {
        console.error = jest.fn();
        mockCreateAclRequest({
          mswInstance: server,
          response: {
            data: { message: "Error message example" },
            status: 400,
          },
        });
      });

      afterEach(() => {
        console.error = originalConsoleError;
        jest.clearAllMocks();
      });

      it("renders an error message", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createAcl", {
          remarks: "",
          aclIpPrincipleType: "PRINCIPAL",
          acl_ssl: ["Alice"],
          aclPatternType: "LITERAL",
          topicname: "aivtopic1",
          environment: "1",
          aclType: "PRODUCER",
          requestOperationType: "CREATE",
          transactionalId: undefined,
          teamId: 1,
        });

        const alert = await screen.findByRole("alert");
        expect(alert).toHaveTextContent("Error message example");

        // it's not important that the console.error is called,
        // but it makes sure that 1) the console.error does not
        // show up in the test logs while 2) flagging an error
        // in case a console.error with a different message
        // gets called - which could be hinting to a problem
        expect(console.error).toHaveBeenCalledWith({
          data: { message: "Error message example" },
          status: 400,
          statusText: "Bad Request",
          headers: {
            map: { "x-powered-by": "msw", "content-type": "application/json" },
          },
        });
      });
    });

    describe("enables user to create a new acl request", () => {
      beforeEach(async () => {
        mockCreateAclRequest({
          mswInstance: server,
          response: {
            status: 200,
            data: { success: true, message: "success" },
          },
        });
      });

      afterEach(() => {
        jest.clearAllMocks();
      });

      it("creates a new acl request when input was valid", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createAcl", {
          remarks: "",
          aclIpPrincipleType: "PRINCIPAL",
          acl_ssl: ["Alice"],
          aclPatternType: "LITERAL",
          topicname: "aivtopic1",
          environment: "1",
          aclType: "PRODUCER",
          requestOperationType: "CREATE",
          teamId: 1,
        });
      });

      it("renders errors and does not submit when input was invalid", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => expect(principalsField).not.toBeValid());
        await waitFor(() =>
          expect(screen.getByText("Enter at least one element.")).toBeVisible()
        );

        expect(spyPost).not.toHaveBeenCalled();
        expect(submitButton).toBeEnabled();
      });

      it("shows a dialog informing user that request was successful", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);

        const successModal = await screen.findByRole("dialog");

        expect(successModal).toBeVisible();
        expect(successModal).toHaveTextContent("Acl request successful!");
      });

      it("user can continue to the next page without waiting for redirect in the dialog", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);

        const successModal = await screen.findByRole("dialog");
        const button = within(successModal).getByRole("button", {
          name: "Continue",
        });

        await userEvent.click(button);

        expect(mockedNavigate).toHaveBeenLastCalledWith(
          "/requests/acls?status=CREATED"
        );
      });
    });
  });

  describe("/topic/:topicName/subscribe: Form submission (TopicConsumerForm)", () => {
    beforeEach(async () => {
      dataSetup({ isAivenCluster: false });

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
          customRoutePath: "/topic/aivtopic1/subscribe?env=1",
        }
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    describe("when user cancels form input", () => {
      beforeEach(async () => {
        await assertSkeleton();
        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);
      });

      const getForm = () => {
        return screen.getByRole("form", {
          name: `Request consumer ACL`,
        });
      };

      it("redirects user to the previous page if they click 'Cancel' on empty form", async () => {
        const form = getForm();

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);

        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });

      it('shows a warning dialog if user clicks "Cancel" and has inputs in form', async () => {
        mockedNavigate.mockClear();
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);
        const dialog = screen.getByRole("dialog");

        expect(dialog).toBeVisible();
        expect(dialog).toHaveTextContent("Cancel ACL request?");
        expect(dialog).toHaveTextContent(
          "Do you want to cancel this request? The data added will be lost."
        );

        expect(mockedNavigate).not.toHaveBeenCalled();
      });

      it("brings the user back to the form when they do not cancel", async () => {
        mockedNavigate.mockClear();
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);
        const dialog = screen.getByRole("dialog");

        const returnButton = screen.getByRole("button", {
          name: "Continue with request",
        });

        await userEvent.click(returnButton);

        expect(mockedNavigate).not.toHaveBeenCalled();

        expect(dialog).not.toBeInTheDocument();
      });

      it("redirects user to previous page if they cancel the request", async () => {
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);

        const returnButton = screen.getByRole("button", {
          name: "Cancel request",
        });

        await userEvent.click(returnButton);

        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });
    });

    describe("when API returns an error", () => {
      const originalConsoleError = console.error;
      beforeEach(async () => {
        console.error = jest.fn();
        mockCreateAclRequest({
          mswInstance: server,
          response: {
            data: { message: "Error message example" },
            status: 400,
          },
        });
      });

      afterEach(() => {
        console.error = originalConsoleError;
      });

      it("renders an error message", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();

        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);

        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();

        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));

        expect(screen.getByRole("radio", { name: "Principal" })).toBeChecked();

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        const consumerGroupField = await screen.findByRole("textbox", {
          name: "Consumer group *",
        });

        await userEvent.type(consumerGroupField, "group");
        userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createAcl", {
          remarks: "",
          aclIpPrincipleType: "PRINCIPAL",
          acl_ssl: ["Alice"],
          aclPatternType: "LITERAL",
          topicname: "aivtopic1",
          environment: "1",
          aclType: "CONSUMER",
          teamId: 1,
          consumergroup: "group",
          requestOperationType: "CREATE",
        });

        const alert = await screen.findByRole("alert");
        expect(alert).toHaveTextContent("Error message example");

        // it's not important that the console.error is called,
        // but it makes sure that 1) the console.error does not
        // show up in the test logs while 2) flagging an error
        // in case a console.error with a different message
        // gets called - which could be hinting to a problem
        expect(console.error).toHaveBeenCalledWith({
          data: { message: "Error message example" },
          status: 400,
          statusText: "Bad Request",
          headers: {
            map: { "x-powered-by": "msw", "content-type": "application/json" },
          },
        });
      });
    });

    describe("enables user to create a new acl request", () => {
      beforeEach(async () => {
        mockCreateAclRequest({
          mswInstance: server,
          response: {
            status: 200,
            data: { success: true, message: "success" },
          },
        });
      });

      it("creates a new acl request when input was valid", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();

        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);

        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();

        expect(
          screen.getByRole("radio", { name: "Principal" })
        ).toBeInTheDocument();

        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));
        expect(screen.getByRole("radio", { name: "Principal" })).toBeChecked();

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        const consumerGroupField = await screen.findByRole("textbox", {
          name: "Consumer group *",
        });

        await userEvent.type(consumerGroupField, "group");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createAcl", {
          remarks: "",
          aclIpPrincipleType: "PRINCIPAL",
          acl_ssl: ["Alice"],
          aclPatternType: "LITERAL",
          topicname: "aivtopic1",
          environment: "1",
          aclType: "CONSUMER",
          teamId: 1,
          consumergroup: "group",
          requestOperationType: "CREATE",
          transactionalId: undefined,
        });
      });

      it("renders errors and does not submit when input was invalid", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => expect(principalsField).not.toBeValid());
        await waitFor(() =>
          expect(screen.getByText("Enter at least one element.")).toBeVisible()
        );

        expect(spyPost).not.toHaveBeenCalled();
        expect(submitButton).toBeEnabled();
      });

      it("shows a dialog informing user that request was successful", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();

        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);

        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();

        expect(
          screen.getByRole("radio", { name: "Principal" })
        ).toBeInTheDocument();

        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));
        expect(screen.getByRole("radio", { name: "Principal" })).toBeChecked();

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        const consumerGroupField = await screen.findByRole("textbox", {
          name: "Consumer group *",
        });

        await userEvent.type(consumerGroupField, "group");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        const successModal = await screen.findByRole("dialog");

        expect(successModal).toBeVisible();
        expect(successModal).toHaveTextContent("Acl request successful!");
      });

      it("user can continue to the next page without waiting for redirect in the dialog", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();

        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);

        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();

        expect(
          screen.getByRole("radio", { name: "Principal" })
        ).toBeInTheDocument();

        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));
        expect(screen.getByRole("radio", { name: "Principal" })).toBeChecked();

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        const consumerGroupField = await screen.findByRole("textbox", {
          name: "Consumer group *",
        });

        await userEvent.type(consumerGroupField, "group");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        const successModal = await screen.findByRole("dialog");
        const button = within(successModal).getByRole("button", {
          name: "Continue",
        });

        await userEvent.click(button);

        expect(mockedNavigate).toHaveBeenLastCalledWith(
          "/requests/acls?status=CREATED"
        );
      });
    });
  });

  describe("/request/acl: Form states (producer, consumer)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: true });

      customRender(<TopicAclRequest />, {
        queryClient: true,
        memoryRouter: true,
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

  describe("/request/acl: User interaction (TopicProducerForm)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: false });

      customRender(<TopicAclRequest />, {
        queryClient: true,
        memoryRouter: true,
      });
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

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

      await selectTestEnvironment();

      expect(principalField).toBeEnabled();

      await userEvent.click(principalField);

      expect(principalField).toBeChecked();

      const principalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(principalsField).toBeInTheDocument();
      expect(principalsField).toBeEnabled();

      await userEvent.click(ipField);

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      expect(visibleIpsField).toBeInTheDocument();
      expect(visibleIpsField).toBeEnabled();
    });

    it("error when entering invalid IP in IPs field", async () => {
      await assertSkeleton();

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const ipsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(ipsField, "invalid{Enter}");
      await waitFor(() => expect(ipsField).toBeInvalid());
      await waitFor(() =>
        expect(screen.getByText("Invalid IP address.")).toBeVisible()
      );
    });

    it("does not error when entering valid IP in IPs field", async () => {
      await assertSkeleton();

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "111.111.11.11{Enter}");
      expect(visibleIpsField).toBeValid();
    });

    it("errors when entering more than 15 IPs in IPs field", async () => {
      await assertSkeleton();

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "111.111.11.11{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.12{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.13{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.14{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.15{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.16{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.17{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.18{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.19{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.20{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.21{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.22{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.23{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.24{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.25{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.26{Enter}");

      await waitFor(() => expect(visibleIpsField).not.toBeValid());
      await waitFor(() =>
        expect(screen.getByText("Maximum 15 elements allowed.")).toBeVisible()
      );
    });

    it("errors when entering more than 5 elements in Principal field", async () => {
      await assertSkeleton();

      const principalField = screen.getByRole("radio", { name: "Principal" });

      await selectTestEnvironment();

      expect(principalField).toBeEnabled();

      await userEvent.click(principalField);

      expect(principalField).toBeChecked();

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await userEvent.type(visiblePrincipalsField, "Alice1{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice2{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice3{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice4{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice5{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice6{Enter}");

      await waitFor(() => expect(visiblePrincipalsField).not.toBeValid());
      await waitFor(() =>
        expect(screen.getByText("Maximum 5 elements allowed.")).toBeVisible()
      );
    });

    it("errors when entering a wrong value in Transactional ID field", async () => {
      await assertSkeleton();

      const transactionalIdInput = screen.getByLabelText("Transactional ID");

      await userEvent.type(transactionalIdInput, "Hello invalid");
      await userEvent.tab();

      await waitFor(() => expect(transactionalIdInput).not.toBeValid());
      await waitFor(() =>
        expect(
          screen.getByText("Only characters allowed: a-z, A-Z, 0-9, ., _,-.")
        ).toBeVisible()
      );
    });

    it("errors when entering a too long value in Transactional ID field", async () => {
      await assertSkeleton();

      const transactionalIdInput = screen.getByLabelText("Transactional ID");
      const tooLong = new Array(152).join("a");
      await userEvent.type(transactionalIdInput, tooLong);
      await userEvent.tab();

      await waitFor(() => expect(transactionalIdInput).not.toBeValid());
      await waitFor(() =>
        expect(
          screen.getByText(
            "Transactional ID cannot be more than 150 characters."
          )
        ).toBeVisible()
      );
    });

    it("does not error when entering a correct value in Transactional ID field", async () => {
      await assertSkeleton();

      const transactionalIdInput = screen.getByLabelText("Transactional ID");

      await userEvent.type(transactionalIdInput, "HelloValid");
      await userEvent.tab();

      await waitFor(() => expect(transactionalIdInput).toBeValid());
    });

    it("renders an enabled Submit button when there is an error", async () => {
      await assertSkeleton();

      const transactionalIdInput = screen.getByLabelText("Transactional ID");
      const tooLong = new Array(152).join("a");
      await userEvent.type(transactionalIdInput, tooLong);
      await userEvent.tab();

      expect(
        screen.getByRole("button", { name: "Submit request" })
      ).toBeEnabled();
    });

    it("renders correct fields when selecting Literal or Prefixed in aclPatternType fields", async () => {
      await assertSkeleton();

      await selectTestEnvironment();

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

      await userEvent.click(literalField);

      const visibleTopicNameField = await screen.findByRole("combobox", {
        name: "Topic name *",
      });

      expect(hiddenPrefixField).toBeNull();
      expect(visibleTopicNameField).toBeInTheDocument();
      expect(visibleTopicNameField).toBeEnabled();
      expect(visibleTopicNameField).toHaveDisplayValue("aivtopic1");

      await userEvent.click(prefixedField);

      expect(prefixedField).toBeChecked();

      const visiblePrefixField = await screen.findByRole("textbox", {
        name: "Prefix *",
      });

      expect(hiddenTopicNameField).toBeNull();
      expect(visiblePrefixField).toBeInTheDocument();
      expect(visiblePrefixField).toBeEnabled();
      expect(visiblePrefixField).toHaveDisplayValue("aivtopic1");
    });

    it("navigates back when clicking Cancel (TopicProducerForm)", async () => {
      await assertSkeleton();

      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      await userEvent.click(cancelButton);

      await waitFor(() => {
        expect(mockedNavigate).toHaveBeenCalledTimes(1);
        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });
    });

    it("navigates back when clicking Cancel (TopicConsumerForm)", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });

      await userEvent.click(aclConsumerTypeInput);

      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      await userEvent.click(cancelButton);

      expect(mockedNavigate).toHaveBeenCalledWith(-1);
    });
  });

  describe("/request/acl: User interaction (TopicConsumerForm, NOT Aiven cluster)", () => {
    beforeEach(() => {
      dataSetup({ isAivenCluster: false });

      customRender(<TopicAclRequest />, {
        queryClient: true,
        memoryRouter: true,
      });
    });

    afterEach(cleanup);

    it("renders correct fields when selecting IP or Principal in AclIpPrincipleTypeField", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

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

      await selectTestEnvironment();

      expect(principalField).toBeEnabled();

      await userEvent.click(principalField);

      expect(principalField).toBeChecked();

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });

      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await userEvent.click(ipField);

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
      await userEvent.click(aclConsumerTypeInput);

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "invalid{Enter}");
      await waitFor(() => expect(visibleIpsField).toBeInvalid());
    });

    it("does not error when entering valid IP in IPs field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "111.111.11.11{Enter}");

      await waitFor(() => expect(visibleIpsField).toBeValid());
    });

    it("errors when entering more than 15 IPs in IPs field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const ipField = screen.getByRole("radio", { name: "IP" });

      await selectTestEnvironment();

      expect(ipField).toBeEnabled();

      await userEvent.click(ipField);

      expect(ipField).toBeChecked();

      const visibleIpsField = await screen.findByRole("textbox", {
        name: "IP addresses *",
      });

      await userEvent.type(visibleIpsField, "111.111.11.11{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.12{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.13{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.14{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.15{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.16{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.17{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.18{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.19{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.20{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.21{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.22{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.23{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.24{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.25{Enter}");
      await userEvent.type(visibleIpsField, "111.111.11.26{Enter}");

      await waitFor(() => expect(visibleIpsField).not.toBeValid());
      await waitFor(() =>
        expect(screen.getByText("Maximum 15 elements allowed.")).toBeVisible()
      );
    });

    it("errors when entering more than 5 elements in Principal field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      const principalField = screen.getByRole("radio", { name: "Principal" });

      await selectTestEnvironment();

      expect(principalField).toBeEnabled();

      await userEvent.click(principalField);

      expect(principalField).toBeChecked();

      const visiblePrincipalsField = await screen.findByRole("textbox", {
        name: "SSL DN strings / Usernames *",
      });
      expect(visiblePrincipalsField).toBeInTheDocument();
      expect(visiblePrincipalsField).toBeEnabled();

      await userEvent.type(visiblePrincipalsField, "Alice1{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice2{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice3{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice4{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice5{Enter}");
      await userEvent.type(visiblePrincipalsField, "Alice6{Enter}");

      await waitFor(() => expect(visiblePrincipalsField).not.toBeValid());
      await waitFor(() =>
        expect(screen.getByText("Maximum 5 elements allowed.")).toBeVisible()
      );
    });

    it("errors when entering a wrong value in Consumer Group field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      await selectTestEnvironment();

      const consumerGroupInput = await screen.findByRole("textbox", {
        name: "Consumer group *",
      });

      await userEvent.type(consumerGroupInput, "Hello invalid");
      await userEvent.tab();

      await waitFor(() => expect(consumerGroupInput).not.toBeValid());
      await waitFor(() =>
        expect(
          screen.getByText("Only characters allowed: a-z, A-Z, 0-9, ., _,-.")
        ).toBeVisible()
      );
    });

    it("errors when entering a too long value in Consumer group field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      await selectTestEnvironment();

      const consumerGroupInput = await screen.findByRole("textbox", {
        name: "Consumer group *",
      });
      const tooLong = new Array(152).join("a");
      await userEvent.type(consumerGroupInput, tooLong);
      await userEvent.tab();

      await waitFor(() => expect(consumerGroupInput).not.toBeValid());
      await waitFor(() =>
        expect(
          screen.getByText("Consumer group cannot be more than 150 characters.")
        ).toBeVisible()
      );
    });

    it("does not error when entering a correct value in Consumer group field", async () => {
      await assertSkeleton();

      const aclConsumerTypeInput = screen.getByRole("radio", {
        name: "Consumer",
      });
      await userEvent.click(aclConsumerTypeInput);

      await selectTestEnvironment();

      const consumerGroupInput = await screen.findByRole("textbox", {
        name: "Consumer group *",
      });

      await userEvent.type(consumerGroupInput, "HelloValid");
      await userEvent.tab();

      await waitFor(() => expect(consumerGroupInput).toBeValid());
    });
  });

  describe("/request/acl: Form submission (TopicProducerForm)", () => {
    const locationAssignSpy = jest.fn();
    let originalLocation: Location;

    beforeAll(() => {
      originalLocation = window.location;
      Object.defineProperty(global.window, "location", {
        writable: true,
        value: {
          assign: locationAssignSpy,
        },
      });
    });

    afterAll(() => {
      global.window.location = originalLocation;
    });

    beforeEach(async () => {
      dataSetup({ isAivenCluster: true });

      customRender(<TopicAclRequest />, {
        queryClient: true,
        memoryRouter: true,
      });
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    describe("when user cancels form input", () => {
      beforeEach(async () => {
        await assertSkeleton();
        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Producer",
        });
        await userEvent.click(aclConsumerTypeInput);
      });

      const getForm = () => {
        return screen.getByRole("form", {
          name: `Request producer ACL`,
        });
      };

      it("redirects user to the previous page if they click 'Cancel' on empty form", async () => {
        const form = getForm();

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);

        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });

      it('shows a warning dialog if user clicks "Cancel" and has inputs in form', async () => {
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);
        const dialog = screen.getByRole("dialog");

        expect(dialog).toBeVisible();
        expect(dialog).toHaveTextContent("Cancel ACL request?");
        expect(dialog).toHaveTextContent(
          "Do you want to cancel this request? The data added will be lost."
        );

        expect(mockedNavigate).not.toHaveBeenCalled();
      });

      it("brings the user back to the form when they do not cancel", async () => {
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);
        const dialog = screen.getByRole("dialog");

        const returnButton = screen.getByRole("button", {
          name: "Continue with request",
        });

        await userEvent.click(returnButton);

        expect(mockedNavigate).not.toHaveBeenCalled();

        expect(dialog).not.toBeInTheDocument();
      });

      it("redirects user to previous page if they cancel the request", async () => {
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);

        const returnButton = screen.getByRole("button", {
          name: "Cancel request",
        });

        await userEvent.click(returnButton);

        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });
    });

    describe("when API returns an error", () => {
      const originalConsoleError = console.error;
      beforeEach(async () => {
        console.error = jest.fn();
        mockCreateAclRequest({
          mswInstance: server,
          response: {
            data: { message: "Error message example" },
            status: 400,
          },
        });
      });

      afterEach(() => {
        console.error = originalConsoleError;
      });

      it("renders an error message", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createAcl", {
          remarks: "",
          aclIpPrincipleType: "PRINCIPAL",
          acl_ssl: ["Alice"],
          aclPatternType: "LITERAL",
          topicname: "aivtopic1",
          environment: "1",
          aclType: "PRODUCER",
          requestOperationType: "CREATE",
          transactionalId: undefined,
          teamId: 1,
        });

        const alert = await screen.findByRole("alert");
        expect(alert).toHaveTextContent("Error message example");

        // it's not important that the console.error is called,
        // but it makes sure that 1) the console.error does not
        // show up in the test logs while 2) flagging an error
        // in case a console.error with a different message
        // gets called - which could be hinting to a problem
        expect(console.error).toHaveBeenCalledWith({
          data: { message: "Error message example" },
          status: 400,
          statusText: "Bad Request",
          headers: {
            map: { "x-powered-by": "msw", "content-type": "application/json" },
          },
        });
      });
    });

    describe("enables user to create a new acl request", () => {
      beforeEach(async () => {
        mockCreateAclRequest({
          mswInstance: server,
          response: {
            status: 200,
            data: { success: true, message: "success" },
          },
        });
      });

      it("creates a new acl request when input was valid", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createAcl", {
          remarks: "",
          aclIpPrincipleType: "PRINCIPAL",
          acl_ssl: ["Alice"],
          aclPatternType: "LITERAL",
          topicname: "aivtopic1",
          environment: "1",
          aclType: "PRODUCER",
          requestOperationType: "CREATE",
          teamId: 1,
        });
      });

      it("renders errors and does not submit when input was invalid", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => expect(principalsField).not.toBeValid());
        await waitFor(() =>
          expect(screen.getByText("Enter at least one element.")).toBeVisible()
        );

        expect(spyPost).not.toHaveBeenCalled();
        expect(submitButton).toBeEnabled();
      });

      it("shows a dialog informing user that request was successful", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);

        const successModal = await screen.findByRole("dialog");

        expect(successModal).toBeVisible();
        expect(successModal).toHaveTextContent("Acl request successful!");
      });

      it("user can continue to the next page without waiting for redirect in the dialog", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(
          screen.getByRole("radio", { name: "Service account" })
        );

        const principalsField = await screen.findByRole("combobox", {
          name: "Service accounts *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);

        const successModal = await screen.findByRole("dialog");
        const button = within(successModal).getByRole("button", {
          name: "Continue",
        });

        await userEvent.click(button);

        expect(mockedNavigate).toHaveBeenLastCalledWith(
          "/requests/acls?status=CREATED"
        );
      });
    });
  });

  describe("/request/acl: Form submission (TopicConsumerForm)", () => {
    beforeEach(async () => {
      dataSetup({ isAivenCluster: false });

      customRender(<TopicAclRequest />, {
        queryClient: true,
        memoryRouter: true,
      });
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    describe("when user cancels form input", () => {
      beforeEach(async () => {
        await assertSkeleton();
        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);
      });

      const getForm = () => {
        return screen.getByRole("form", {
          name: `Request consumer ACL`,
        });
      };

      it("redirects user to the previous page if they click 'Cancel' on empty form", async () => {
        const form = getForm();

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);

        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });

      it('shows a warning dialog if user clicks "Cancel" and has inputs in form', async () => {
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);
        const dialog = screen.getByRole("dialog");

        expect(dialog).toBeVisible();
        expect(dialog).toHaveTextContent("Cancel ACL request?");
        expect(dialog).toHaveTextContent(
          "Do you want to cancel this request? The data added will be lost."
        );

        expect(mockedNavigate).not.toHaveBeenCalled();
      });

      it("brings the user back to the form when they do not cancel", async () => {
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);
        const dialog = screen.getByRole("dialog");

        const returnButton = screen.getByRole("button", {
          name: "Continue with request",
        });

        await userEvent.click(returnButton);

        expect(mockedNavigate).not.toHaveBeenCalled();

        expect(dialog).not.toBeInTheDocument();
      });

      it("redirects user to previous page if they cancel the request", async () => {
        const form = getForm();

        const remarkInput = screen.getByRole("textbox", {
          name: "Remarks",
        });
        await userEvent.type(remarkInput, "Important information");

        const button = within(form).getByRole("button", {
          name: "Cancel",
        });

        await userEvent.click(button);

        const returnButton = screen.getByRole("button", {
          name: "Cancel request",
        });

        await userEvent.click(returnButton);

        expect(mockedNavigate).toHaveBeenCalledWith(-1);
      });
    });

    describe("when API returns an error", () => {
      const originalConsoleError = console.error;
      beforeEach(async () => {
        console.error = jest.fn();
        mockCreateAclRequest({
          mswInstance: server,
          response: {
            data: { message: "Error message example" },
            status: 400,
          },
        });
      });

      afterEach(() => {
        console.error = originalConsoleError;
      });

      it("renders an error message", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();

        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);

        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();

        const visibleTopicNameField = await screen.findByRole("combobox", {
          name: "Topic name *",
        });

        await userEvent.selectOptions(
          visibleTopicNameField,
          mockedResponseTopicNames[0]
        );

        expect(visibleTopicNameField).toHaveValue(mockedResponseTopicNames[0]);

        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));

        expect(screen.getByRole("radio", { name: "Principal" })).toBeChecked();

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        const consumerGroupField = await screen.findByRole("textbox", {
          name: "Consumer group *",
        });

        await userEvent.type(consumerGroupField, "group");
        userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createAcl", {
          remarks: "",
          aclIpPrincipleType: "PRINCIPAL",
          acl_ssl: ["Alice"],
          aclPatternType: "LITERAL",
          topicname: mockedResponseTopicNames[0],
          environment: "1",
          aclType: "CONSUMER",
          teamId: 1,
          consumergroup: "group",
          requestOperationType: "CREATE",
        });

        const alert = await screen.findByRole("alert");
        expect(alert).toHaveTextContent("Error message example");

        // it's not important that the console.error is called,
        // but it makes sure that 1) the console.error does not
        // show up in the test logs while 2) flagging an error
        // in case a console.error with a different message
        // gets called - which could be hinting to a problem
        expect(console.error).toHaveBeenCalledWith({
          data: { message: "Error message example" },
          status: 400,
          statusText: "Bad Request",
          headers: {
            map: { "x-powered-by": "msw", "content-type": "application/json" },
          },
        });
      });
    });

    describe("enables user to create a new acl request", () => {
      beforeEach(async () => {
        mockCreateAclRequest({
          mswInstance: server,
          response: {
            status: 200,
            data: { success: true, message: "success" },
          },
        });
      });

      it("creates a new acl request when input was valid", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();

        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);

        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();

        const visibleTopicNameField = await screen.findByRole("combobox", {
          name: "Topic name *",
        });

        await userEvent.selectOptions(
          visibleTopicNameField,
          mockedResponseTopicNames[0]
        );

        expect(visibleTopicNameField).toHaveValue(mockedResponseTopicNames[0]);

        expect(
          screen.getByRole("radio", { name: "Principal" })
        ).toBeInTheDocument();

        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));
        expect(screen.getByRole("radio", { name: "Principal" })).toBeChecked();

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        const consumerGroupField = await screen.findByRole("textbox", {
          name: "Consumer group *",
        });

        await userEvent.type(consumerGroupField, "group");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        expect(spyPost).toHaveBeenCalledWith("/createAcl", {
          remarks: "",
          aclIpPrincipleType: "PRINCIPAL",
          acl_ssl: ["Alice"],
          aclPatternType: "LITERAL",
          topicname: mockedResponseTopicNames[0],
          environment: "1",
          aclType: "CONSUMER",
          teamId: 1,
          consumergroup: "group",
          requestOperationType: "CREATE",
          transactionalId: undefined,
        });
      });

      it("renders errors and does not submit when input was invalid", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();
        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        await selectTestEnvironment();
        await userEvent.click(screen.getByRole("radio", { name: "Literal" }));
        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => expect(principalsField).not.toBeValid());
        await waitFor(() =>
          expect(screen.getByText("Enter at least one element.")).toBeVisible()
        );

        expect(spyPost).not.toHaveBeenCalled();
        expect(submitButton).toBeEnabled();
      });

      it("shows a dialog informing user that request was successful", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();

        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);

        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();

        const visibleTopicNameField = await screen.findByRole("combobox", {
          name: "Topic name *",
        });

        await userEvent.selectOptions(
          visibleTopicNameField,
          mockedResponseTopicNames[0]
        );

        expect(visibleTopicNameField).toHaveValue(mockedResponseTopicNames[0]);

        expect(
          screen.getByRole("radio", { name: "Principal" })
        ).toBeInTheDocument();

        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));
        expect(screen.getByRole("radio", { name: "Principal" })).toBeChecked();

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        const consumerGroupField = await screen.findByRole("textbox", {
          name: "Consumer group *",
        });

        await userEvent.type(consumerGroupField, "group");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        const successModal = await screen.findByRole("dialog");

        expect(successModal).toBeVisible();
        expect(successModal).toHaveTextContent("Acl request successful!");
      });

      it("user can continue to the next page without waiting for redirect in the dialog", async () => {
        const spyPost = jest.spyOn(api, "post");
        await assertSkeleton();

        const aclConsumerTypeInput = screen.getByRole("radio", {
          name: "Consumer",
        });
        await userEvent.click(aclConsumerTypeInput);

        const submitButton = screen.getByRole("button", {
          name: "Submit request",
        });

        // Fill form with valid data
        await selectTestEnvironment();

        const visibleTopicNameField = await screen.findByRole("combobox", {
          name: "Topic name *",
        });

        await userEvent.selectOptions(
          visibleTopicNameField,
          mockedResponseTopicNames[0]
        );

        expect(visibleTopicNameField).toHaveValue(mockedResponseTopicNames[0]);

        expect(
          screen.getByRole("radio", { name: "Principal" })
        ).toBeInTheDocument();

        await userEvent.click(screen.getByRole("radio", { name: "Principal" }));
        expect(screen.getByRole("radio", { name: "Principal" })).toBeChecked();

        const principalsField = await screen.findByRole("textbox", {
          name: "SSL DN strings / Usernames *",
        });

        expect(principalsField).toBeVisible();
        expect(principalsField).toBeEnabled();

        await userEvent.type(principalsField, "Alice");
        await userEvent.tab();

        const consumerGroupField = await screen.findByRole("textbox", {
          name: "Consumer group *",
        });

        await userEvent.type(consumerGroupField, "group");
        await userEvent.tab();

        await waitFor(() => expect(submitButton).toBeEnabled());
        await userEvent.click(submitButton);

        await waitFor(() => {
          expect(submitButton).toBeDisabled();
        });

        expect(spyPost).toHaveBeenCalledTimes(1);
        const successModal = await screen.findByRole("dialog");
        const button = within(successModal).getByRole("button", {
          name: "Continue",
        });

        await userEvent.click(button);

        expect(mockedNavigate).toHaveBeenLastCalledWith(
          "/requests/acls?status=CREATED"
        );
      });
    });
  });
});
