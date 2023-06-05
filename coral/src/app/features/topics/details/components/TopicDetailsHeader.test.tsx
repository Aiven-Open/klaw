import { cleanup, render, screen } from "@testing-library/react";
import { TopicDetailsHeader } from "src/app/features/topics/details/components/TopicDetailsHeader";
import { EnvironmentInfo } from "src/domain/environment";
import { within } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";

const mockedUsedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

const testTopicName = "my-nice-topic";
const testEnvironments: EnvironmentInfo[] = [
  { id: "1", name: "DEV" },
  { id: "2", name: "TST" },
];
const mockSetEnvironmentId = jest.fn();

const defaultProps = {
  topicName: testTopicName,
  environments: testEnvironments,
  environmentId: "1",
  setEnvironmentId: mockSetEnvironmentId,
  topicExists: true,
};
describe("TopicDetailsHeader", () => {
  const user = userEvent.setup();

  describe("handles loading state when environments are not yet given", () => {
    beforeAll(() => {
      render(<TopicDetailsHeader {...defaultProps} environments={undefined} />);
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
      render(<TopicDetailsHeader {...defaultProps} topicExists={false} />);
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
      render(<TopicDetailsHeader {...defaultProps} />);
    });

    afterAll(cleanup);

    it("shows the topic name", () => {
      const name = screen.getByRole("heading", { name: testTopicName });

      expect(name).toBeVisible();
    });

    it("shows a select element for environments", () => {
      const select = screen.getByRole("combobox", {
        name: "Select environment",
      });

      expect(select).toBeEnabled();
    });

    it("shows the first element from environments as the selected one", () => {
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
      render(<TopicDetailsHeader {...defaultProps} />);
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
