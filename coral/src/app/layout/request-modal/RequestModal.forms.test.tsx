import { Context as AquariumContext } from "@aivenio/aquarium";
import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import {
  createMemoryRouter,
  Link,
  Outlet,
  RouteObject,
  RouterProvider,
  useLocation,
} from "react-router-dom";
import TopicAclRequest from "src/app/features/topics/acl-request/TopicAclRequest";
import ConnectorRequest from "src/app/features/connectors/request/ConnectorRequest";
import ConnectorEditRequest from "src/app/features/connectors/request/ConnectorEditRequest";
import { ConnectorPromotionRequest } from "src/app/features/connectors/request/ConnectorPromotionRequest";
import TopicEditRequest from "src/app/features/topics/request/TopicEditRequest";
import TopicPromotionRequest from "src/app/features/topics/request/TopicPromotionRequest";
import TopicRequest from "src/app/features/topics/request/TopicRequest";
import { TopicSchemaRequest } from "src/app/features/topics/schema-request/TopicSchemaRequest";
import { getMockedResponseGetClusterInfoFromEnvironment } from "src/domain/cluster/cluster-api-test-helper";
import { getClusterInfoFromEnvironment } from "src/domain/cluster";
import { getConnectorDetailsPerEnv } from "src/domain/connector";
import { getAllEnvironmentsForConnector } from "src/domain/environment";
import {
  getAllEnvironmentsForTopicAndAcl,
  getEnvironmentsForTopicRequest,
} from "src/domain/environment/environment-api";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import {
  getTopicAdvancedConfigOptions,
  getTopicDetailsPerEnv,
  getTopicNames,
  getTopicTeam,
} from "src/domain/topic/topic-api";
import {
  mockedResponseTopicNames,
  mockedResponseTopicTeamLiteral,
} from "src/domain/topic/topic-test-helper";
import { getQueryClientForTests } from "src/services/test-utils/query-client-tests";
import { requestModalRoute } from "src/app/layout/request-modal/request-modal-route";
import { Routes } from "src/services/router-utils/types";

// Forms report their dirty state to the request modal. These tests render
// the real forms inside the real RequestModal (via requestModalRoute) to
// make sure an untouched form is not dirty, i.e. Cancel closes it without
// the "Do you want to cancel this request?" warning.

jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/topic/topic-api.ts");
jest.mock("src/domain/cluster/cluster-api.ts");
jest.mock("src/domain/connector/connector-api.ts");
jest.mock("src/domain/schema-request/schema-request-api.ts");
jest.mock("src/domain/acl/acl-api", () => ({
  ...jest.requireActual("src/domain/acl/acl-api"),
  getAivenServiceAccounts: jest.fn().mockReturnValue(["account"]),
}));

const mockedEnvironments = [
  createMockEnvironmentDTO({ name: "TST", id: "1" }),
  createMockEnvironmentDTO({ name: "DEV", id: "2" }),
];

const warningText =
  "Do you want to cancel this request? The data added will be lost.";

function LocationDisplay() {
  const location = useLocation();
  return <div data-testid="location">{location.pathname}</div>;
}

type FormCase = {
  name: string;
  route: RouteObject;
  /** the url the user opens the modal with */
  url: string;
  /** waits until the form has loaded its data */
  waitForForm: () => Promise<unknown>;
  /** changes something in the form, like a user would */
  changeForm: () => Promise<void>;
  /**
   * Whether the form is clean again after a change was undone. Not the case
   * for the ACL forms: values set programmatically (consumergroup "-na-",
   * the Aiven principal type) and transactionalId ("" once a field was
   * edited, sending "" to the API when used as default) differ from the
   * defaults after the first edit.
   */
  cleanAfterUndo: boolean;
};

function renderModalFromOriginPage(formCase: FormCase) {
  const router = createMemoryRouter(
    [
      {
        path: "/",
        element: (
          <>
            <LocationDisplay />
            <Outlet />
          </>
        ),
        children: [
          {
            path: "/origin",
            element: <Link to={formCase.url}>Open request modal</Link>,
          },
          formCase.route,
        ],
      },
    ],
    { initialEntries: ["/origin"] }
  );

  render(
    <QueryClientProvider client={getQueryClientForTests()}>
      <AquariumContext>
        <RouterProvider router={router} />
      </AquariumContext>
    </QueryClientProvider>
  );
}

const formCases: FormCase[] = [
  {
    name: "topic request",
    route: requestModalRoute({
      path: Routes.TOPIC_REQUEST,
      element: <TopicRequest />,
      title: "Request topic",
      confirmTitle: "Cancel topic request?",
    }),
    url: Routes.TOPIC_REQUEST,
    waitForForm: () => screen.findByRole("option", { name: "DEV" }),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Topic name/ }),
        "my-topic"
      ),
    cleanAfterUndo: true,
  },
  {
    name: "connector request",
    route: requestModalRoute({
      path: Routes.CONNECTOR_REQUEST,
      element: <ConnectorRequest />,
      title: "Request connector",
      confirmTitle: "Cancel connector request?",
    }),
    url: Routes.CONNECTOR_REQUEST,
    waitForForm: () => screen.findByRole("option", { name: "DEV" }),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Connector name/ }),
        "my-connector"
      ),
    cleanAfterUndo: true,
  },
  {
    name: "schema request",
    route: requestModalRoute({
      path: Routes.SCHEMA_REQUEST,
      element: <TopicSchemaRequest />,
      title: "Request new schema",
      confirmTitle: "Cancel schema request?",
    }),
    url: Routes.SCHEMA_REQUEST,
    waitForForm: () => screen.findByRole("option", { name: "DEV" }),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: true,
  },
  {
    name: "ACL request (producer)",
    route: requestModalRoute({
      path: Routes.ACL_REQUEST,
      element: <TopicAclRequest />,
      title: "ACL (Access Control) request",
      confirmTitle: "Cancel ACL request?",
    }),
    url: Routes.ACL_REQUEST,
    waitForForm: () => screen.findByRole("option", { name: "DEV" }),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: false,
  },
  {
    name: "ACL request (consumer)",
    route: requestModalRoute({
      path: Routes.ACL_REQUEST,
      element: <TopicAclRequest />,
      title: "ACL (Access Control) request",
      confirmTitle: "Cancel ACL request?",
    }),
    url: Routes.ACL_REQUEST,
    waitForForm: async () => {
      await userEvent.click(
        await screen.findByRole("radio", { name: "Consumer" })
      );
      return screen.findByRole("option", { name: "DEV" });
    },
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: false,
  },
  {
    name: "ACL request for a topic on an Aiven cluster (presets from the url)",
    route: requestModalRoute({
      path: Routes.TOPIC_ACL_REQUEST,
      element: <TopicAclRequest />,
      title: "Subscribe to topic",
      confirmTitle: "Cancel ACL request?",
    }),
    url: "/topic/aivtopic1/subscribe?env=1",
    waitForForm: () => screen.findByRole("radio", { name: "Service account" }),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: false,
  },
  {
    name: "schema request for a topic",
    route: requestModalRoute({
      path: Routes.TOPIC_SCHEMA_REQUEST,
      element: <TopicSchemaRequest />,
      title: "Request new schema for topic",
      confirmTitle: "Cancel schema request?",
    }),
    url: "/topic/aivtopic1/request-schema?env=1",
    waitForForm: () =>
      waitFor(() =>
        expect(
          screen.getByRole("combobox", { name: /Environment/i })
        ).toHaveDisplayValue("TST")
      ),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: true,
  },
  {
    name: "topic edit request",
    route: requestModalRoute({
      path: Routes.TOPIC_EDIT_REQUEST,
      element: <TopicEditRequest />,
      title: "Request topic update",
      confirmTitle: "Cancel topic update?",
    }),
    url: "/topic/test-topic-name/request-update?env=1",
    waitForForm: () => screen.findByDisplayValue("Topic description"),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: true,
  },
  {
    name: "topic promotion request",
    route: requestModalRoute({
      path: Routes.TOPIC_PROMOTION_REQUEST,
      element: <TopicPromotionRequest />,
      title: "Request topic promotion",
      confirmTitle: "Cancel topic promotion?",
    }),
    url: "/topic/test-topic-name/request-promotion?sourceEnv=1&targetEnv=2",
    waitForForm: () => screen.findByDisplayValue("Topic description"),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: true,
  },
  {
    name: "connector edit request",
    route: requestModalRoute({
      path: Routes.CONNECTOR_EDIT_REQUEST,
      element: <ConnectorEditRequest />,
      title: "Request connector update",
      confirmTitle: "Cancel connector update?",
    }),
    url: "/connector/test-connector/request-update?env=4",
    waitForForm: () => screen.findByDisplayValue("Connector description"),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: true,
  },
  {
    name: "connector promotion request",
    route: requestModalRoute({
      path: Routes.CONNECTOR_PROMOTION_REQUEST,
      element: <ConnectorPromotionRequest />,
      title: "Request connector promotion",
      confirmTitle: "Cancel connector promotion?",
    }),
    url: "/connector/test-connector/request-promotion?sourceEnv=111&targetEnv=999",
    waitForForm: () => screen.findByDisplayValue("Connector description"),
    changeForm: () =>
      userEvent.type(
        screen.getByRole("textbox", { name: /Message for approval/ }),
        "hello"
      ),
    cleanAfterUndo: true,
  },
];

describe("request forms inside the request modal", () => {
  const user = userEvent.setup();

  beforeEach(() => {
    jest.clearAllMocks();
    (getEnvironmentsForTopicRequest as jest.Mock).mockResolvedValue(
      mockedEnvironments
    );
    (getTopicAdvancedConfigOptions as jest.Mock).mockResolvedValue([]);
    (getAllEnvironmentsForConnector as jest.Mock).mockResolvedValue([
      createMockEnvironmentDTO({ name: "DEV", id: "2" }),
      createMockEnvironmentDTO({ name: "SOURCE", id: "111" }),
      createMockEnvironmentDTO({ name: "TARGET", id: "999" }),
    ]);
    (getTopicDetailsPerEnv as jest.Mock).mockResolvedValue({
      topicExists: true,
      topicId: "1200",
      topicContents: {
        topicName: "test-topic-name",
        noOfPartitions: 2,
        description: "Topic description",
        noOfReplicas: "1",
        teamId: 0,
        showEditTopic: true,
        showDeleteTopic: false,
        topicDeletable: false,
        advancedTopicConfiguration: { "cleanup.policy": "compact" },
      },
    });
    (getConnectorDetailsPerEnv as jest.Mock).mockResolvedValue({
      connectorExists: true,
      connectorId: 1005,
      connectorContents: {
        connectorName: "test-connector",
        environmentId: "4",
        environmentName: "DEV",
        connectorConfig: '{\n  "tasks.max" : "1"\n}',
        description: "Connector description",
      },
    });
    // the schema request only lists environments with an associated schema environment
    (getAllEnvironmentsForTopicAndAcl as jest.Mock).mockResolvedValue([
      createMockEnvironmentDTO({
        name: "TST",
        id: "1",
        associatedEnv: { id: "9", name: "TST_SCH" },
      }),
      createMockEnvironmentDTO({
        name: "DEV",
        id: "2",
        associatedEnv: { id: "3", name: "DEV_SCH" },
      }),
    ]);
    (getTopicNames as jest.Mock).mockResolvedValue(mockedResponseTopicNames);
    (getTopicTeam as jest.Mock).mockResolvedValue(
      mockedResponseTopicTeamLiteral
    );
    (getClusterInfoFromEnvironment as jest.Mock).mockResolvedValue(
      getMockedResponseGetClusterInfoFromEnvironment(true)
    );
  });

  afterEach(cleanup);

  describe.each(formCases)("$name", (formCase) => {
    async function openModal() {
      renderModalFromOriginPage(formCase);
      await user.click(
        screen.getByRole("link", { name: "Open request modal" })
      );
      await formCase.waitForForm();
    }

    it("closes without the cancel warning when nothing was changed", async () => {
      await openModal();

      await user.click(screen.getByRole("button", { name: "Cancel" }));

      expect(screen.queryByText(warningText)).not.toBeInTheDocument();
      expect(screen.getByTestId("location")).toHaveTextContent("/origin");
    });

    // Editing a field makes react-hook-form compare ALL values with the
    // default values. After undoing the edit, the form must be clean again,
    // which only works when the default values are complete and correct.
    const itIfCleanAfterUndo = formCase.cleanAfterUndo ? it : it.skip;
    itIfCleanAfterUndo(
      "closes without the cancel warning when a change was undone",
      async () => {
        await openModal();

        const field = screen.getByRole("textbox", {
          name: /Message for approval/,
        });
        await user.type(field, "a");
        await user.clear(field);
        await user.click(screen.getByRole("button", { name: "Cancel" }));

        expect(screen.queryByText(warningText)).not.toBeInTheDocument();
        expect(screen.getByTestId("location")).toHaveTextContent("/origin");
      }
    );

    it("asks for confirmation before closing when the user changed the form", async () => {
      await openModal();

      await formCase.changeForm();
      await user.click(screen.getByRole("button", { name: "Cancel" }));

      expect(await screen.findByText(warningText)).toBeVisible();
      expect(screen.getByTestId("location")).not.toHaveTextContent("/origin");
    });
  });
});
