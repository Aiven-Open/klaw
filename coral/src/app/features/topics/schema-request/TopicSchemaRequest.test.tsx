import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react/pure";
import { within } from "@testing-library/react";
import { TopicSchemaRequest } from "src/app/features/topics/schema-request/TopicSchemaRequest";
import { server } from "src/services/api-mocks/server";
import { mockGetSchemaRegistryEnvironments } from "src/domain/environment/environment-api.msw";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import * as ReactQuery from "@tanstack/react-query";

const useQuerySpy = jest.spyOn(ReactQuery, "useQuery");

const testTopicName = "my-awesome-topic";

const mockedEnvironments = [
  { name: "DEV", id: "1" },
  { name: "TST", id: "2" },
  { name: "INFRA", id: "3" },
];
const mockedGetSchemaRegistryEnvironments = mockedEnvironments.map((entry) => {
  return createMockEnvironmentDTO(entry);
});

describe("TopicSchemaRequest", () => {
  beforeAll(() => {
    server.listen();
  });

  afterAll(() => {
    server.close();
  });

  describe("handles loading and update states", () => {
    const getForm = () => {
      return screen.getByRole("form", {
        name: `Request a new schema`,
      });
    };

    beforeAll(() => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      useQuerySpy.mockReturnValue({ data: [], isLoading: true });
      customRender(<TopicSchemaRequest topicName={testTopicName} />, {
        queryClient: true,
      });
    });

    afterAll(() => {
      cleanup();
      useQuerySpy.mockRestore();
    });

    it("shows a form to request a schema", () => {
      const form = getForm();

      expect(form).toBeVisible();
    });

    it("shows a loading element while environments are being fetched", () => {
      const form = getForm();
      const loadingEnvironments = within(form).getByTestId(
        "environments-select-loading"
      );

      expect(loadingEnvironments).toBeVisible();
    });
  });

  describe("renders all necessary elements", () => {
    const getForm = () => {
      return screen.getByRole("form", {
        name: `Request a new schema`,
      });
    };

    beforeAll(async () => {
      mockGetSchemaRegistryEnvironments({
        mswInstance: server,
        response: { data: mockedGetSchemaRegistryEnvironments },
      });
      customRender(<TopicSchemaRequest topicName={testTopicName} />, {
        queryClient: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("environments-select-loading")
      );
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

    it("renders all options for environment based on api data", () => {
      const form = getForm();
      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const options = within(select).getAllByRole("option");

      expect(options).toHaveLength(mockedEnvironments.length + 1);
    });

    mockedEnvironments.forEach((env) => {
      it(`renders a option ${env.name} with the environments id set as value ${env.id}`, () => {
        const form = getForm();
        const select = within(form).getByRole("combobox", {
          name: /Select environment/i,
        });
        const option = within(select).getByRole("option", { name: env.name });

        expect(option).toBeVisible();
        expect(option).toHaveValue(env.id);
      });
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
