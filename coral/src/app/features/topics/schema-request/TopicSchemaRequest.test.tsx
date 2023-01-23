import { cleanup, screen } from "@testing-library/react/pure";
import { render, within } from "@testing-library/react";
import { TopicSchemaRequest } from "src/app/features/topics/schema-request/TopicSchemaRequest";

// // mock out svgs to avoid clutter
// jest.mock("@aivenio/aquarium", () => {
//   return {
//     __esModule: true,
//     ...jest.requireActual("@aivenio/aquarium"),
//     Icon: () => null,
//   };
// });

const testTopicName = "my-awesome-topic";

describe("TopicSchemaRequest", () => {
  describe("renders all necessary elements", () => {
    const getForm = () => {
      return screen.getByRole("form", {
        name: `Request a new schema`,
      });
    };
    beforeAll(() => {
      render(<TopicSchemaRequest topicName={testTopicName} />);
    });

    afterAll(() => {
      cleanup();
    });

    it("shows a form to request a schema", () => {
      const form = getForm();

      expect(form).toBeVisible();
    });

    it("shows a required select element to choose an environment", () => {
      const form = getForm();
      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });

      expect(select).toBeEnabled();
      expect(select).toBeRequired();
    });

    it("shows readonly select element for the topic name", () => {
      const form = getForm();
      const select = within(form).getByRole("combobox", { name: "Topic name" });

      expect(select).toBeVisible();
      expect(select).toBeDisabled();
      expect(select).toHaveAttribute("aria-readonly", "true");
    });

    it("sets the value for readonly select for topic name", () => {
      const form = getForm();
      const select = within(form).getByRole("combobox", { name: "Topic name" });

      expect(select).toHaveValue(testTopicName);
    });

    it("shows a required file upload input for AVRO Schema", () => {
      const form = getForm();
      // <input type="file" /> does not have a corresponding role
      const fileUpload =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      expect(fileUpload).toBeEnabled();
      expect(fileUpload).toBeRequired();
    });

    it("shows an optional text input for remarks", () => {
      const form = getForm();
      // <input type="file" /> does not have a corresponding role
      const textArea = within(form).getByRole("textbox", {
        name: "Message for the approval",
      });

      expect(textArea).toBeEnabled();
      expect(textArea).not.toBeRequired();
    });

    it("shows a disabled button to submit the form", () => {
      const form = getForm();
      // <input type="file" /> does not have a corresponding role
      const button = within(form).getByRole("button", {
        name: "Submit request",
      });

      expect(button).toBeDisabled();
    });

    it("shows an enabled button to cancel form input", () => {
      const form = getForm();
      // <input type="file" /> does not have a corresponding role
      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      expect(button).toBeEnabled();
    });
  });
});
