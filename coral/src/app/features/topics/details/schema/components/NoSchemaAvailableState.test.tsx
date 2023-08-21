import { cleanup, screen } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";
import { NoSchemaAvailableState } from "src/app/features/topics/details/schema/components/NoSchemaAvailableState";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const testTopicName = "my-little-topic";
describe("NoSchemaAvailableState", () => {
  const user = userEvent.setup();

  describe('renders view when "topicSchemasIsRefetching" is true', () => {
    describe("for user that is topicOwner", () => {
      beforeAll(() => {
        customRender(
          <NoSchemaAvailableState
            topicName={testTopicName}
            isTopicOwner={true}
            topicSchemasIsRefetching={true}
            createSchemaAllowed={false}
          />,
          { memoryRouter: true }
        );
      });
      afterAll(cleanup);

      it("shows a headline", () => {
        const headline = screen.getByRole("heading", { name: "Schema" });

        expect(headline).toBeVisible();
      });

      it("shows information that no schema is available", () => {
        const headline = screen.getByRole("heading", {
          name: "No schema available for this topic",
        });

        expect(headline).toBeVisible();
      });

      it("shows a disabled button to request a new schema", () => {
        const button = screen.getByRole("button", {
          name: "Request a new schema",
        });

        expect(button).toBeDisabled();
      });

      it("shows no promotable only alert", () => {
        const alert = screen.queryByTestId("schema-promotable-only-alert");

        expect(alert).not.toBeInTheDocument();
      });
    });

    describe("for user that is not topicOwner", () => {
      beforeAll(() => {
        customRender(
          <NoSchemaAvailableState
            topicName={testTopicName}
            isTopicOwner={false}
            topicSchemasIsRefetching={true}
            createSchemaAllowed={false}
          />,
          { memoryRouter: true }
        );
      });
      afterAll(cleanup);

      it("shows a headline", () => {
        const headline = screen.getByRole("heading", { name: "Schema" });

        expect(headline).toBeVisible();
      });

      it("shows information that no schema is available", () => {
        const headline = screen.getByRole("heading", {
          name: "No schema available for this topic",
        });

        expect(headline).toBeVisible();
      });

      it("shows a no button to request a new schema", () => {
        const button = screen.queryByRole("button");

        expect(button).not.toBeInTheDocument();
      });

      it("shows no promotable only alert", () => {
        const alert = screen.queryByTestId("schema-promotable-only-alert");

        expect(alert).not.toBeInTheDocument();
      });
    });
  });

  describe('renders view when "createSchemaAllowed" is true', () => {
    describe("for user that is topicOwner", () => {
      beforeAll(() => {
        customRender(
          <NoSchemaAvailableState
            topicName={testTopicName}
            isTopicOwner={true}
            topicSchemasIsRefetching={false}
            createSchemaAllowed={true}
          />,
          { memoryRouter: true }
        );
      });
      afterAll(cleanup);

      it("shows a headline", () => {
        const headline = screen.getByRole("heading", { name: "Schema" });

        expect(headline).toBeVisible();
      });

      it("shows information that no schema is available", () => {
        const headline = screen.getByRole("heading", {
          name: "No schema available for this topic",
        });

        expect(headline).toBeVisible();
      });

      it("shows a button to request a new schema", () => {
        const button = screen.getByRole("button", {
          name: "Request a new schema",
        });

        expect(button).toBeEnabled();
      });

      it("shows no promotable only alert", () => {
        const alert = screen.queryByTestId("schema-promotable-only-alert");

        expect(alert).not.toBeInTheDocument();
      });
    });

    describe("enables user that are topicOwner to request a new schema ", () => {
      beforeEach(() => {
        customRender(
          <NoSchemaAvailableState
            topicName={testTopicName}
            isTopicOwner={true}
            topicSchemasIsRefetching={false}
            createSchemaAllowed={true}
          />,
          { memoryRouter: true }
        );
      });
      afterEach(() => {
        cleanup();
      });

      it("navigates user to right page when clicking 'Request new schema'", async () => {
        const button = screen.getByRole("button", {
          name: "Request a new schema",
        });

        await user.click(button);

        expect(mockedNavigate).toHaveBeenCalledWith(
          `/topic/${testTopicName}/request-schema`
        );
      });
    });

    describe("for user that is not topicOwner", () => {
      beforeAll(() => {
        customRender(
          <NoSchemaAvailableState
            topicName={testTopicName}
            isTopicOwner={false}
            topicSchemasIsRefetching={false}
            createSchemaAllowed={true}
          />,
          { memoryRouter: true }
        );
      });
      afterAll(cleanup);

      it("shows a headline", () => {
        const headline = screen.getByRole("heading", { name: "Schema" });

        expect(headline).toBeVisible();
      });

      it("shows information that no schema is available", () => {
        const headline = screen.getByRole("heading", {
          name: "No schema available for this topic",
        });

        expect(headline).toBeVisible();
      });

      it("shows no button to request a new schema", () => {
        const button = screen.queryByRole("button");

        expect(button).not.toBeInTheDocument();
      });

      it("shows no promotable only alert", () => {
        const alert = screen.queryByTestId("schema-promotable-only-alert");

        expect(alert).not.toBeInTheDocument();
      });
    });
  });

  describe('renders view when "createSchemaAllowed" is false', () => {
    describe("for user that is topicOwner", () => {
      beforeAll(() => {
        customRender(
          <NoSchemaAvailableState
            topicName={testTopicName}
            isTopicOwner={true}
            topicSchemasIsRefetching={false}
            createSchemaAllowed={false}
          />,
          { memoryRouter: true }
        );
      });
      afterAll(cleanup);

      it("shows a headline", () => {
        const headline = screen.getByRole("heading", { name: "Schema" });

        expect(headline).toBeVisible();
      });

      it("shows information that no schema is available", () => {
        const headline = screen.getByRole("heading", {
          name: "No schema available for this topic",
        });

        expect(headline).toBeVisible();
      });

      it("shows a disabled button to request a new schema", () => {
        const button = screen.getByRole("button", {
          name: "Request a new schema",
        });

        expect(button).toBeDisabled();
      });

      it("shows the promotable only alert", () => {
        const alert = screen.getByTestId("schema-promotable-only-alert");

        expect(alert).toBeVisible();
      });
    });

    describe("for user that is not topicOwner", () => {
      beforeAll(() => {
        customRender(
          <NoSchemaAvailableState
            topicName={testTopicName}
            isTopicOwner={false}
            topicSchemasIsRefetching={false}
            createSchemaAllowed={false}
          />,
          { memoryRouter: true }
        );
      });
      afterAll(cleanup);

      it("shows a headline", () => {
        const headline = screen.getByRole("heading", { name: "Schema" });

        expect(headline).toBeVisible();
      });

      it("shows information that no schema is available", () => {
        const headline = screen.getByRole("heading", {
          name: "No schema available for this topic",
        });

        expect(headline).toBeVisible();
      });

      it("shows no button to request a new schema", () => {
        const button = screen.queryByRole("button");

        expect(button).not.toBeInTheDocument();
      });

      it("shows no promotable only alert", () => {
        const alert = screen.queryByTestId("schema-promotable-only-alert");

        expect(alert).not.toBeInTheDocument();
      });
    });
  });
});
