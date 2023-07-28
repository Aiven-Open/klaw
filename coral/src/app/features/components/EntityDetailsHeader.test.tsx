import { cleanup, screen } from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { EntityDetailsHeader } from "src/app/features/components/EntityDetailsHeader";
import { EnvironmentInfo } from "src/domain/environment";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedUsedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

const testTopic = {
  name: "my-nice-topic",
  type: "topic" as "topic" | "connector",
};
const testConnector = {
  name: "my-nice-connector",
  type: "connector" as "topic" | "connector",
};
const testEnvironments: EnvironmentInfo[] = [
  { id: "1", name: "DEV" },
  { id: "2", name: "TST" },
];
const mockSetEnvironmentId = jest.fn();

const defaultTopicProps = {
  entity: testTopic,
  entityEditLink: "/hello/topic",
  showEditButton: true,
  environments: testEnvironments,
  environmentId: undefined,
  setEnvironmentId: mockSetEnvironmentId,
  entityExists: true,
  entityUpdating: false,
};

const defaultConnectorProps = {
  entity: testConnector,
  entityEditLink: "/hello/connector",
  showEditButton: true,
  environments: testEnvironments,
  environmentId: undefined,
  setEnvironmentId: mockSetEnvironmentId,
  entityExists: true,
  entityUpdating: false,
};

describe("EntityDetailsHeader", () => {
  describe("handles header for details view for topic", () => {
    const user = userEvent.setup();

    describe("handles loading state when environments are not yet given", () => {
      beforeAll(() => {
        customRender(
          <EntityDetailsHeader
            {...defaultTopicProps}
            environments={undefined}
          />,
          { browserRouter: true }
        );
      });

      afterAll(cleanup);

      it("shows a disabled select", () => {
        const select = screen.getByRole("combobox");

        expect(select).toBeDisabled();
      });

      it("shows a placeholder 'Loading' for the select", () => {
        const select = screen.getByRole("combobox");

        expect(select).toHaveAttribute("placeholder", "Loading");
      });

      it("shows no information about amount of environments", () => {
        const environmentAmountInfo = screen.queryByText("Environment", {
          exact: false,
        });

        expect(environmentAmountInfo).not.toBeInTheDocument();
      });
    });

    describe("handles a non existent topic", () => {
      beforeAll(() => {
        customRender(
          <EntityDetailsHeader {...defaultTopicProps} entityExists={false} />,
          { browserRouter: true }
        );
      });

      afterAll(cleanup);

      it("shows no select element when topic does not exist", () => {
        const select = screen.queryByRole("combobox");

        expect(select).not.toBeInTheDocument();
      });

      it("shows the link to edit topic as disabled", () => {
        const link = screen.getByRole("link", { name: "Edit topic" });

        expect(link).toBeDisabled();
      });

      it("shows no information about amount of environments", () => {
        const environmentAmountInfo = screen.queryByText("Environment", {
          exact: false,
        });

        expect(environmentAmountInfo).not.toBeInTheDocument();
      });
    });

    describe("shows all necessary elements if topic exists", () => {
      beforeAll(() => {
        customRender(<EntityDetailsHeader {...defaultTopicProps} />, {
          browserRouter: true,
        });
      });

      afterAll(cleanup);

      it("shows the topic name", () => {
        const name = screen.getByRole("heading", {
          name: defaultTopicProps.entity.name,
        });

        expect(name).toBeVisible();
      });

      it("shows a select element for environments", () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });

        expect(select).toBeEnabled();
      });

      it("sets environmentId to be the first element from environments if it is undefined on load", () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });

        expect(select).toHaveValue(testEnvironments[0].id);
        expect(select).toHaveDisplayValue(testEnvironments[0].name);
      });

      it("shows all given environments as options", () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });
        const options = within(select).getAllByRole("option");

        expect(options[0]).toHaveValue(testEnvironments[0].id);
        expect(options[0]).toHaveTextContent(testEnvironments[0].name);
        expect(options[1]).toHaveValue(testEnvironments[1].id);
        expect(options[1]).toHaveTextContent(testEnvironments[1].name);
      });

      it("shows information about the amount of environments", () => {
        const info = screen.getByText(
          `${testEnvironments.length} Environments`
        );

        expect(info).toBeVisible();
      });

      it("shows a link to edit the topic", () => {
        const link = screen.getByRole("link", { name: "Edit topic" });

        expect(link).toBeVisible();
        expect(link).toHaveAttribute("href", defaultTopicProps.entityEditLink);
      });
    });

    describe("disables button to edit as long as data is updated", () => {
      beforeAll(() => {
        customRender(
          <EntityDetailsHeader {...defaultTopicProps} entityUpdating={true} />,
          { browserRouter: true }
        );
      });

      afterAll(cleanup);

      it("shows a link to edit the topic", () => {
        const link = screen.getByRole("link", { name: "Edit topic" });

        expect(link).toBeDisabled();
      });
    });

    describe("handles user selecting an environment", () => {
      beforeEach(() => {
        customRender(<EntityDetailsHeader {...defaultTopicProps} />, {
          browserRouter: true,
        });
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it('sets the environment id for environment "TST"', async () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });

        await user.selectOptions(select, "TST");

        expect(mockSetEnvironmentId).toHaveBeenCalledWith("2");
      });

      it('sets the environment id for environment "DEV"', async () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });

        await user.selectOptions(select, "DEV");

        expect(mockSetEnvironmentId).toHaveBeenCalledWith("1");
      });
    });
  });

  describe("handles header for details view for connector", () => {
    const user = userEvent.setup();

    describe("handles loading state when environments are not yet given", () => {
      beforeAll(() => {
        customRender(
          <EntityDetailsHeader
            {...defaultConnectorProps}
            environments={undefined}
          />,
          { browserRouter: true }
        );
      });

      afterAll(cleanup);

      it("shows a disabled select", () => {
        const select = screen.getByRole("combobox");

        expect(select).toBeDisabled();
      });

      it("shows a placeholder 'Loading' for the select", () => {
        const select = screen.getByRole("combobox");

        expect(select).toHaveAttribute("placeholder", "Loading");
      });

      it("shows no information about amount of environments", () => {
        const environmentAmountInfo = screen.queryByText("Environment", {
          exact: false,
        });

        expect(environmentAmountInfo).not.toBeInTheDocument();
      });
    });

    describe("handles a non existent connector", () => {
      beforeAll(() => {
        customRender(
          <EntityDetailsHeader
            {...defaultConnectorProps}
            entityExists={false}
          />,
          { browserRouter: true }
        );
      });

      afterAll(cleanup);

      it("shows no select element when connector does not exist", () => {
        const select = screen.queryByRole("combobox");

        expect(select).not.toBeInTheDocument();
      });

      it("shows the link to edit connector as disabled", () => {
        const link = screen.getByRole("link", { name: "Edit connector" });

        expect(link).toBeDisabled();
      });

      it("shows no information about amount of environments", () => {
        const environmentAmountInfo = screen.queryByText("Environment", {
          exact: false,
        });

        expect(environmentAmountInfo).not.toBeInTheDocument();
      });
    });

    describe("shows all necessary elements if connector exists", () => {
      beforeAll(() => {
        customRender(<EntityDetailsHeader {...defaultConnectorProps} />, {
          browserRouter: true,
        });
      });

      afterAll(cleanup);

      it("shows the connector name", () => {
        const name = screen.getByRole("heading", {
          name: defaultConnectorProps.entity.name,
        });

        expect(name).toBeVisible();
      });

      it("shows a select element for environments", () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });

        expect(select).toBeEnabled();
      });

      it("sets environmentId to be the first element from environments if it is undefined on load", () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });

        expect(select).toHaveValue(testEnvironments[0].id);
        expect(select).toHaveDisplayValue(testEnvironments[0].name);
      });

      it("shows all given environments as options", () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });
        const options = within(select).getAllByRole("option");

        expect(options[0]).toHaveValue(testEnvironments[0].id);
        expect(options[0]).toHaveTextContent(testEnvironments[0].name);
        expect(options[1]).toHaveValue(testEnvironments[1].id);
        expect(options[1]).toHaveTextContent(testEnvironments[1].name);
      });

      it("shows information about the amount of environments", () => {
        const info = screen.getByText(
          `${testEnvironments.length} Environments`
        );

        expect(info).toBeVisible();
      });

      it("shows a link to edit the connector", () => {
        const link = screen.getByRole("link", { name: "Edit connector" });

        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          defaultConnectorProps.entityEditLink
        );
      });
    });

    describe("disables button to edit as long as data is updated", () => {
      beforeAll(() => {
        customRender(
          <EntityDetailsHeader
            {...defaultConnectorProps}
            entityUpdating={true}
          />,
          { browserRouter: true }
        );
      });

      afterAll(cleanup);

      it("shows a disabled link to edit the topic", () => {
        const link = screen.getByRole("link", { name: "Edit connector" });

        expect(link).toBeDisabled();
      });
    });

    describe("handles user selecting an environment", () => {
      beforeEach(() => {
        customRender(<EntityDetailsHeader {...defaultConnectorProps} />, {
          browserRouter: true,
        });
      });

      afterEach(() => {
        jest.resetAllMocks();
        cleanup();
      });

      it('sets the environment id for environment "TST"', async () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });

        await user.selectOptions(select, "TST");

        expect(mockSetEnvironmentId).toHaveBeenCalledWith("2");
      });

      it('sets the environment id for environment "DEV"', async () => {
        const select = screen.getByRole("combobox", {
          name: "Select environment",
        });

        await user.selectOptions(select, "DEV");

        expect(mockSetEnvironmentId).toHaveBeenCalledWith("1");
      });
    });
  });

  describe("correctly hides Edit button", () => {
    it("hides Edit button for Topic", async () => {
      customRender(
        <EntityDetailsHeader {...defaultTopicProps} showEditButton={false} />,
        { browserRouter: true }
      );

      const button = screen.queryByRole("button", { name: "Edit topic" });

      expect(button).not.toBeInTheDocument();
    });

    it("hides Edit button for Connector", async () => {
      customRender(
        <EntityDetailsHeader
          {...defaultConnectorProps}
          showEditButton={false}
        />,
        { browserRouter: true }
      );

      const button = screen.queryByRole("button", { name: "Edit connector" });

      expect(button).not.toBeInTheDocument();
    });
  });
});
