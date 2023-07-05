import { cleanup, render, screen } from "@testing-library/react";
import { EnvironmentInfo } from "src/domain/environment";
import { within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import { EntityDetailsHeader } from "src/app/features/components/EntityDetailsHeader";

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
  environments: testEnvironments,
  environmentId: undefined,
  setEnvironmentId: mockSetEnvironmentId,
  entityExists: true,
};
const defaultConnectorProps = {
  entity: testConnector,
  entityEditLink: "/hello/connector",
  environments: testEnvironments,
  environmentId: undefined,
  setEnvironmentId: mockSetEnvironmentId,
  entityExists: true,
};

describe("EntityDetailsHeader (topic)", () => {
  const user = userEvent.setup();

  describe("handles loading state when environments are not yet given", () => {
    beforeAll(() => {
      render(
        <EntityDetailsHeader {...defaultTopicProps} environments={undefined} />
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
      render(
        <EntityDetailsHeader {...defaultTopicProps} entityExists={false} />
      );
    });

    afterAll(cleanup);

    it("shows no select element when topic does not exist", () => {
      const select = screen.queryByRole("combobox");

      expect(select).not.toBeInTheDocument();
    });

    it("shows the button to edit topic as disabled", () => {
      const button = screen.getByRole("button", { name: "Edit topic" });

      expect(button).toBeDisabled();
    });

    it("shows no information about amount of environments", () => {
      const environmentAmountInfo = screen.queryByText("Environment", {
        exact: false,
      });

      expect(environmentAmountInfo).not.toBeInTheDocument();
    });
  });

  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(<EntityDetailsHeader {...defaultTopicProps} />);
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
      const info = screen.getByText(`${testEnvironments.length} Environments`);

      expect(info).toBeVisible();
    });

    it("shows a button to edit the topic", () => {
      const button = screen.getByRole("button", { name: "Edit topic" });

      expect(button).toBeEnabled();
    });
  });

  describe("handles user selecting an environment", () => {
    beforeEach(() => {
      render(<EntityDetailsHeader {...defaultTopicProps} />);
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

describe("EntityDetailsHeader (connector)", () => {
  const user = userEvent.setup();

  describe("handles loading state when environments are not yet given", () => {
    beforeAll(() => {
      render(
        <EntityDetailsHeader
          {...defaultConnectorProps}
          environments={undefined}
        />
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
      render(
        <EntityDetailsHeader {...defaultConnectorProps} entityExists={false} />
      );
    });

    afterAll(cleanup);

    it("shows no select element when connector does not exist", () => {
      const select = screen.queryByRole("combobox");

      expect(select).not.toBeInTheDocument();
    });

    it("shows the button to edit connector as disabled", () => {
      const button = screen.getByRole("button", { name: "Edit connector" });

      expect(button).toBeDisabled();
    });

    it("shows no information about amount of environments", () => {
      const environmentAmountInfo = screen.queryByText("Environment", {
        exact: false,
      });

      expect(environmentAmountInfo).not.toBeInTheDocument();
    });
  });

  describe("shows all necessary elements", () => {
    beforeAll(() => {
      render(<EntityDetailsHeader {...defaultConnectorProps} />);
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
      const info = screen.getByText(`${testEnvironments.length} Environments`);

      expect(info).toBeVisible();
    });

    it("shows a button to edit the connector", () => {
      const button = screen.getByRole("button", { name: "Edit connector" });

      expect(button).toBeEnabled();
    });
  });

  describe("handles user selecting an environment", () => {
    beforeEach(() => {
      render(<EntityDetailsHeader {...defaultConnectorProps} />);
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
