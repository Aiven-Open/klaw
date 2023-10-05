import { Context as AquariumContext } from "@aivenio/aquarium";
import { cleanup, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ConnectorSettings } from "src/app/features/connectors/details/settings/ConnectorSettings";
import {
  ConnectorOverview,
  requestConnectorDeletion,
} from "src/domain/connector";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { KlawApiModel } from "types/utils";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const mockedUseConnectorDetails = jest.fn();
jest.mock("src/app/features/connectors/details/ConnectorDetails", () => ({
  useConnectorDetails: () => mockedUseConnectorDetails(),
}));

jest.mock("src/domain/connector/connector-api.ts");
const mockDeleteConnector = requestConnectorDeletion as jest.MockedFunction<
  typeof requestConnectorDeletion
>;

const testConnectorName = "my-nice-connector";
const testEnvironmentId = "8";
const testConnectorInfo: KlawApiModel<"KafkaConnectorModelResponse"> = {
  connectorId: 1003,
  connectorName: testConnectorName,
  runningTasks: 0,
  failedTasks: 0,
  environmentId: testEnvironmentId,
  teamName: "Ospo",
  teamId: 0,
  showEditConnector: true,
  showDeleteConnector: true,
  connectorDeletable: true,
  connectorOwner: true,
  highestEnv: true,
  hasOpenRequest: false,
  hasOpenClaimRequest: false,
  hasOpenRequestOnAnyEnv: false,
  connectorConfig:
    '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "release240",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
  environmentName: "DEV",
  connectorStatus: "statusplaceholder",
};
const testConnectorOverview: ConnectorOverview = {
  connectorInfo: testConnectorInfo,
  connectorHistoryList: [],
  promotionDetails: {
    sourceEnv: "4",
    connectorName: "release240",
    targetEnvId: "10",
    sourceConnectorConfig:
      '{\n  "connector.class" : "io.confluent.connect.storage.tools.SchemaSourceConnector",\n  "tasks.max" : "1",\n  "name" : "release240",\n  "topic" : "testtopic",\n  "topics.regex" : "*"\n}',
    targetEnv: "TST",
    status: "SUCCESS",
  },
  connectorExists: true,
  availableEnvironments: [
    {
      id: "4",
      name: "DEV",
    },
  ],
  connectorIdForDocumentation: 1003,
};
const mockConnectorDetails = {
  connectorName: "my-nice-connector",
  environmentId: "8",
  connectorOverview: testConnectorOverview,
  connectorIsRefetching: false,
  connectorSchemasIsRefetching: false,
};
describe("ConnectorSettings", () => {
  const user = userEvent.setup();

  describe("shows information if user is not allowed to Request connector deletion", () => {
    beforeAll(() => {
      mockDeleteConnector.mockImplementation(jest.fn());
      mockedUseConnectorDetails.mockReturnValue({
        ...mockConnectorDetails,
        connectorOverview: {
          ...testConnectorOverview,
          connectorInfo: { ...testConnectorInfo, connectorOwner: false },
        },
      });

      customRender(
        <AquariumContext>
          <ConnectorSettings />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterAll(cleanup);

    it("shows a page headline", () => {
      const pageHeadline = screen.getByRole("heading", { name: "Settings" });

      expect(pageHeadline).toBeVisible();
    });

    it("shows no headline for the danger zone", () => {
      const dangerHeadline = screen.queryByRole("heading", {
        name: "Danger zone",
      });

      expect(dangerHeadline).not.toBeInTheDocument();
    });

    it("shows no button to delete the connector", () => {
      const button = screen.queryByRole("button", {
        name: "Request connector deletion",
      });

      expect(button).not.toBeInTheDocument();
    });

    it("shows information that settings are only available for users of a team", () => {
      const information = screen.getByText(
        "Settings can only be edited by team members of the team the connector does belong" +
          " to."
      );

      expect(information).toBeVisible();
    });
  });

  describe("shows information if user is allowed to delete but connector is not deletable at the moment", () => {
    describe("informs user that connector is not deletable because there are active running tasks", () => {
      beforeAll(() => {
        mockDeleteConnector.mockImplementation(jest.fn());
        mockedUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetails,
          connectorOverview: {
            ...testConnectorOverview,
            connectorInfo: {
              ...testConnectorInfo,
              showDeleteConnector: false,
              runningTasks: 1,
            },
          },
        });

        customRender(
          <AquariumContext>
            <ConnectorSettings />
          </AquariumContext>,
          {
            memoryRouter: true,
            queryClient: true,
          }
        );
      });

      afterAll(cleanup);

      it("shows the headline for the danger zone", () => {
        const dangerHeadline = screen.getByRole("heading", {
          name: "Danger zone",
        });

        expect(dangerHeadline).toBeVisible();
      });

      it("shows information connector can not be deleted at the moment", () => {
        const information = screen.getByText(
          "You can not create a delete request for this connector:"
        );

        expect(information).toBeVisible();
      });

      it("shows information that connector has open ACL requests", () => {
        const information = screen.getByText(
          "The connector has running tasks. Please wait until they are done before deleting the connector."
        );

        expect(information).toBeVisible();
      });

      it("shows a disabled button to delete the connector", () => {
        const button = screen.getByRole("button", {
          name: "Request connector deletion",
        });

        expect(button).toBeDisabled();
      });
    });

    describe("informs user that connector  is not deletable because it is on a higher environment", () => {
      beforeAll(() => {
        mockDeleteConnector.mockImplementation(jest.fn());
        mockedUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetails,
          connectorOverview: {
            ...testConnectorOverview,
            connectorInfo: {
              ...testConnectorInfo,
              showDeleteConnector: false,
              highestEnv: false,
            },
          },
        });

        customRender(
          <AquariumContext>
            <ConnectorSettings />
          </AquariumContext>,
          {
            memoryRouter: true,
            queryClient: true,
          }
        );
      });

      afterAll(cleanup);

      it("shows the headline for the danger zone", () => {
        const dangerHeadline = screen.getByRole("heading", {
          name: "Danger zone",
        });

        expect(dangerHeadline).toBeVisible();
      });

      it("shows information connector can not be deleted at the moment", () => {
        const information = screen.getByText(
          "You can not create a delete request for this connector:"
        );

        expect(information).toBeVisible();
      });

      it("shows information that connector exists on a higher environment", () => {
        const reasonsList = screen.getByRole("list");
        const listItem = within(reasonsList).getAllByRole("listitem");

        expect(listItem[0]).toHaveTextContent(
          "The connector is on a higher environment. Please delete the connector from that environment first."
        );
        expect(listItem).toHaveLength(1);
      });

      it("shows a disabled button to delete the connector", () => {
        const button = screen.getByRole("button", {
          name: "Request connector deletion",
        });

        expect(button).toBeDisabled();
      });
    });

    describe("informs user that connector is not deletable because of a pending request", () => {
      beforeAll(() => {
        mockDeleteConnector.mockImplementation(jest.fn());
        mockedUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetails,
          connectorOverview: {
            ...testConnectorOverview,
            connectorInfo: {
              ...testConnectorInfo,
              showDeleteConnector: false,
              hasOpenRequest: true,
            },
          },
        });

        customRender(
          <AquariumContext>
            <ConnectorSettings />
          </AquariumContext>,
          {
            memoryRouter: true,
            queryClient: true,
          }
        );
      });

      afterAll(cleanup);

      it("shows the headline for the danger zone", () => {
        const dangerHeadline = screen.getByRole("heading", {
          name: "Danger zone",
        });

        expect(dangerHeadline).toBeVisible();
      });

      it("shows information connector can not be deleted at the moment", () => {
        const information = screen.getByText(
          "You can not create a delete request for this connector:"
        );

        expect(information).toBeVisible();
      });

      it("shows information that connector has a pending request", () => {
        const reasonsList = screen.getByRole("list");
        const listItem = within(reasonsList).getAllByRole("listitem");

        expect(listItem[0]).toHaveTextContent(
          "The connector has a pending request."
        );
        expect(listItem).toHaveLength(1);
      });

      it("shows a disabled button to delete the connector", () => {
        const button = screen.getByRole("button", {
          name: "Request connector deletion",
        });

        expect(button).toBeDisabled();
      });
    });

    describe("informs user that connector is not deletable because multiple reasons", () => {
      beforeAll(() => {
        mockDeleteConnector.mockImplementation(jest.fn());
        mockedUseConnectorDetails.mockReturnValue({
          ...mockConnectorDetails,
          connectorOverview: {
            ...testConnectorOverview,
            connectorInfo: {
              ...testConnectorInfo,
              showDeleteConnector: false,
              hasOpenRequest: true,
              runningTasks: 1,
            },
          },
        });

        customRender(
          <AquariumContext>
            <ConnectorSettings />
          </AquariumContext>,
          {
            memoryRouter: true,
            queryClient: true,
          }
        );
      });

      afterAll(cleanup);

      it("shows information connector can not be deleted at the moment", () => {
        const information = screen.getByText(
          "You can not create a delete request for this connector:"
        );

        expect(information).toBeVisible();
      });

      it("shows information that connector has a pending request and open ACL requests", () => {
        const reasonsList = screen.getByRole("list");
        const listItem = within(reasonsList).getAllByRole("listitem");

        expect(listItem[0]).toHaveTextContent(
          "The connector has running tasks. Please wait until they are done before deleting the connector."
        );

        expect(listItem[1]).toHaveTextContent(
          "The connector has a pending request."
        );

        expect(listItem).toHaveLength(2);
      });
    });
  });

  describe("renders all necessary elements if user can Request connector deletion and it is deletable", () => {
    beforeAll(() => {
      mockDeleteConnector.mockImplementation(jest.fn());
      mockedUseConnectorDetails.mockReturnValue(mockConnectorDetails);

      customRender(
        <AquariumContext>
          <ConnectorSettings />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterAll(cleanup);

    it("shows a page headline", () => {
      const pageHeadline = screen.getByRole("heading", { name: "Settings" });

      expect(pageHeadline).toBeVisible();
    });

    it("shows a headline for the danger zone", () => {
      const dangerHeadline = screen.getByRole("heading", {
        name: "Danger zone",
      });

      expect(dangerHeadline).toBeVisible();
    });

    it("shows a headline for Request connector deletion", () => {
      const deleteConnectorHeadline = screen.getByRole("heading", {
        name: "Request connector deletion",
      });

      expect(deleteConnectorHeadline).toBeVisible();
    });

    it("shows a warning text about deletion of the connector", () => {
      const warningText = screen.getByText(
        "Submit a request for this topic to be deleted. Once the request is approved, the action is irreversible."
      );

      expect(warningText).toBeVisible();
    });

    it("shows a button to delete the connector", () => {
      const button = screen.getByRole("button", {
        name: "Request connector deletion",
      });

      expect(button).toBeVisible();
    });
  });

  describe("shows information about refetching state", () => {
    beforeAll(() => {
      mockDeleteConnector.mockImplementation(jest.fn());
      mockedUseConnectorDetails.mockReturnValue({
        ...mockConnectorDetails,
        connectorIsRefetching: true,
        connectorOverview: {
          ...testConnectorOverview,
          connectorInfo: {
            ...testConnectorInfo,
            showDeleteConnector: false,
            runningTasks: 1,
          },
        },
      });

      customRender(
        <AquariumContext>
          <ConnectorSettings />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterAll(cleanup);

    it("shows a SR only text", () => {
      const loadingInformation = screen.getByText("Loading information");

      expect(loadingInformation).toBeVisible();
      expect(loadingInformation).toHaveClass("visually-hidden");
    });

    it("does not show information why user can not delete request", () => {
      const noDeleteText = screen.queryByText(
        "You can not create a delete request for this connector"
      );

      expect(noDeleteText).not.toBeInTheDocument();
    });

    it("does not show headline and text with information about deletion", () => {
      const deleteHeadline = screen.queryByRole("heading", {
        name: "Request connector deletion",
      });
      const deleteInformation = screen.queryByText(
        "Submit a request for this topic to be deleted. Once the request is approved, the action is irreversible."
      );

      expect(deleteHeadline).not.toBeInTheDocument();
      expect(deleteInformation).not.toBeInTheDocument();
    });

    it("disables the button to delete a connector", () => {
      const deleteButton = screen.getByRole("button", {
        name: "Request connector deletion",
        hidden: true,
      });

      expect(deleteButton).toBeDisabled();
    });
  });

  describe("enables user to delete a connector", () => {
    const originalConsoleError = console.error;

    beforeEach(() => {
      console.error = jest.fn();
      mockDeleteConnector.mockImplementation(jest.fn());
      mockedUseConnectorDetails.mockReturnValue(mockConnectorDetails);

      customRender(
        <AquariumContext>
          <ConnectorSettings />
        </AquariumContext>,
        {
          memoryRouter: true,
          queryClient: true,
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
      console.error = originalConsoleError;
    });

    it('shows a confirmation modal when user clicks "Request connector deletion"', async () => {
      const confirmationModalBeforeClick = screen.queryByRole("dialog");
      expect(confirmationModalBeforeClick).not.toBeInTheDocument();

      const button = screen.getByRole("button", {
        name: "Request connector deletion",
      });

      await user.click(button);
      const confirmationModal = screen.getByRole("dialog");

      expect(confirmationModal).toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it('removes modal and does not Request connector deletion if user clicks "cancel"', async () => {
      const button = screen.getByRole("button", {
        name: "Request connector deletion",
      });

      await user.click(button);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const cancelButton = within(dialog).getByRole("button", {
        name: "Cancel",
      });

      await user.click(cancelButton);

      expect(dialog).not.toBeInTheDocument();
      expect(mockedNavigate).not.toHaveBeenCalled();
      expect(mockDeleteConnector).not.toHaveBeenCalled();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("deletes connector successfully when user confirms deleting", async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      mockDeleteConnector.mockResolvedValue({ success: true });

      const button = screen.getByRole("button", {
        name: "Request connector deletion",
      });

      await user.click(button);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const confirmButton = within(dialog).getByRole("button", {
        name: "Request connector deletion",
      });

      await user.click(confirmButton);

      expect(mockDeleteConnector).toHaveBeenCalledWith({
        envId: testEnvironmentId,
        connectorName: testConnectorName,
        remark: undefined,
      });
      expect(mockedNavigate).toHaveBeenCalledWith("/connectors");
      expect(dialog).not.toBeVisible();
      expect(console.error).not.toHaveBeenCalled();
    });

    it("shows a message if deleting the connector resulted in an error", async () => {
      mockDeleteConnector.mockRejectedValue({
        success: false,
        message: "Oh no error",
      });

      const button = screen.getByRole("button", {
        name: "Request connector deletion",
      });

      await user.click(button);

      const dialog = screen.getByRole("dialog");
      expect(dialog).toBeVisible();

      const confirmButton = within(dialog).getByRole("button", {
        name: "Request connector deletion",
      });

      await user.click(confirmButton);

      expect(mockDeleteConnector).toHaveBeenCalledWith({
        envId: testEnvironmentId,
        connectorName: testConnectorName,
      });

      expect(mockedNavigate).not.toHaveBeenCalled();
      expect(dialog).not.toBeVisible();

      const errorMessage = screen.getByRole("alert");

      expect(errorMessage).toBeVisible();
      expect(errorMessage).toHaveTextContent("Oh no error");
      expect(console.error).toHaveBeenCalledWith({
        message: "Oh no error",
        success: false,
      });
    });
  });
});
