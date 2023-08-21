import { NoSchemaBanner } from "src/app/features/topics/details/schema/components/NoSchemaBanner";
import { render, cleanup, screen } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const testTopicName = "my-topic";
describe("NoSchemaBanner", () => {
  const user = userEvent.setup();

  describe("renders right view when user is not topic owner", () => {
    beforeAll(() => {
      render(
        <NoSchemaBanner
          topicName={testTopicName}
          isTopicOwner={false}
          schemaIsRefetching={false}
          isCreatingSchemaAllowed={false}
        />
      );
    });

    afterAll(cleanup);

    it("shows information to user", () => {
      const info = screen.getByText("No schema available for this topic");

      expect(info).toBeVisible();
    });

    it("does not render a button to request a schema", () => {
      const button = screen.queryByRole("button");

      expect(button).not.toBeInTheDocument();
    });
  });

  describe("renders right view when user is topic owner", () => {
    describe("when schemas are being refetched", () => {
      beforeAll(() => {
        render(
          <NoSchemaBanner
            topicName={testTopicName}
            isTopicOwner={true}
            schemaIsRefetching={true}
            isCreatingSchemaAllowed={true}
          />
        );
      });

      afterAll(cleanup);

      it("shows information that there is no schema yet", () => {
        const text = screen.getByText("No schema available for this topic");

        expect(text).toBeVisible();
      });

      it("shows disabled button to request a new schema", () => {
        const button = screen.getByRole("button", {
          name: "Request a new schema",
        });

        expect(button).toBeDisabled();
      });
    });

    describe("when user is allowed to create a schema directly", () => {
      beforeAll(() => {
        render(
          <NoSchemaBanner
            topicName={testTopicName}
            isTopicOwner={true}
            schemaIsRefetching={false}
            isCreatingSchemaAllowed={true}
          />
        );
      });

      afterAll(cleanup);

      it("shows information that there is no schema yet", () => {
        const text = screen.getByText("No schema available for this topic");

        expect(text).toBeVisible();
      });

      it("shows button to request a new schema", () => {
        const button = screen.getByRole("button", {
          name: "Request a new schema",
        });

        expect(button).toBeEnabled();
      });
    });

    describe("when user can not create a schema directly but it has to promote", () => {
      beforeAll(() => {
        render(
          <NoSchemaBanner
            topicName={testTopicName}
            isTopicOwner={true}
            schemaIsRefetching={false}
            isCreatingSchemaAllowed={false}
          />
        );
      });

      afterAll(cleanup);

      it("shows information that there is no schema yet", () => {
        const text = screen.getByText("No schema available for this topic");

        expect(text).toBeVisible();
      });

      it("disables button to request a new schema", () => {
        const button = screen.getByRole("button", {
          name: "Request a new schema",
        });

        expect(button).toBeDisabled();
      });

      it("shows information that schema has to be promoted", () => {
        const alert = screen.getByTestId("schema-promotable-only-alert");

        expect(alert).toBeInTheDocument();
        expect(alert).toHaveTextContent(
          "To add a schema to this topic, create a request in the lower environment"
        );
      });
    });
  });

  describe("enables topic owner to request a new schema", () => {
    beforeEach(() => {
      customRender(
        <NoSchemaBanner
          topicName={testTopicName}
          isTopicOwner={true}
          schemaIsRefetching={false}
          isCreatingSchemaAllowed={true}
        />,
        {
          memoryRouter: true,
        }
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("navigates user to correct form", async () => {
      const button = screen.getByRole("button", {
        name: "Request a new schema",
      });
      await user.click(button);

      expect(mockedNavigate).toHaveBeenCalledWith(
        `/topic/${testTopicName}/request-schema`
      );
    });
  });
});
