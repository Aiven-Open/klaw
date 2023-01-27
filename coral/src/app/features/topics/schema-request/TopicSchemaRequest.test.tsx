import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react/pure";
import { waitFor, within } from "@testing-library/react";
import { TopicSchemaRequest } from "src/app/features/topics/schema-request/TopicSchemaRequest";
import { createMockEnvironmentDTO } from "src/domain/environment/environment-test-helper";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import * as ReactQuery from "@tanstack/react-query";
import userEvent from "@testing-library/user-event";
import { getSchemaRegistryEnvironments } from "src/domain/environment";
import { createSchemaRequest } from "src/domain/schema-request";
import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { getTopicNames } from "src/domain/topic";

jest.mock("src/domain/schema-request/schema-request-api.ts");
jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/topic/topic-api.ts");

const mockGetSchemaRegistryEnvironments =
  getSchemaRegistryEnvironments as jest.MockedFunction<
    typeof getSchemaRegistryEnvironments
  >;
const mockCreateSchemaRequest = createSchemaRequest as jest.MockedFunction<
  typeof createSchemaRequest
>;
const mockGetTopicNames = getTopicNames as jest.MockedFunction<
  typeof getTopicNames
>;

const mockedUsedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

const useQuerySpy = jest.spyOn(ReactQuery, "useQuery");

const testTopicName = "my-awesome-topic";

const mockedEnvironments = [
  { name: "DEV", id: "1" },
  { name: "TST", id: "2" },
  { name: "INFRA", id: "3" },
];
const mockedGetSchemaRegistryEnvironments = transformEnvironmentApiResponse(
  mockedEnvironments.map((entry) => {
    return createMockEnvironmentDTO(entry);
  })
);

const fileName = "my-awesome-schema.avsc";
const testFile: File = new File(["{}"], fileName, {
  type: "image/jpeg",
});

describe("TopicSchemaRequest", () => {
  const getForm = () => {
    return screen.getByRole("form", {
      name: `Request a new schema`,
    });
  };

  afterEach(() => {
    jest.resetAllMocks();
  });

  describe("checks if topicName passed from url is part of topics user can request schemas for", () => {
    beforeEach(() => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedGetSchemaRegistryEnvironments
      );
      mockCreateSchemaRequest.mockImplementation(jest.fn());
    });

    afterEach(() => {
      cleanup();
      useQuerySpy.mockRestore();
    });

    it("does not redirect user if topicName prop does is part of list of topicNames", async () => {
      mockGetTopicNames.mockResolvedValue([
        "topic-1",
        "topic-2",
        testTopicName,
      ]);

      customRender(<TopicSchemaRequest topicName={testTopicName} />, {
        queryClient: true,
      });

      const form = getForm();
      expect(form).toBeVisible();

      // only testing this would always return green - even waitFor will return always
      // true, since the mock is not called directly, so waitFor will check, confirm
      // it has not been called because it has not happened yet. Checking for the
      // form makes implicitly sure that navigate was not called (otherwise no form)
      // and this assertion is mostly for readability
      expect(mockedUsedNavigate).not.toHaveBeenCalledWith("/topics");
    });

    it("redirects user if topicName prop does not exist in list of topicNames", async () => {
      mockGetTopicNames.mockResolvedValue(["topic-1", "topic-2"]);

      customRender(<TopicSchemaRequest topicName={testTopicName} />, {
        queryClient: true,
      });

      await waitFor(() => {
        expect(mockedUsedNavigate).toHaveBeenCalledWith("/topics");
      });
    });
  });

  describe("handles loading and update state", () => {
    beforeAll(() => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      //@ts-ignore
      useQuerySpy.mockReturnValue({ data: undefined, isLoading: true });
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
      mockCreateSchemaRequest.mockImplementation(jest.fn());
      mockGetTopicNames.mockResolvedValue([testTopicName]);

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
    beforeAll(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedGetSchemaRegistryEnvironments
      );
      mockCreateSchemaRequest.mockImplementation(jest.fn());
      mockGetTopicNames.mockResolvedValue([testTopicName]);

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

    test.each(mockedEnvironments)(
      `renders a option $name with the environments id set as value $id`,
      ({ name, id }) => {
        const form = getForm();
        const select = within(form).getByRole("combobox", {
          name: /Select environment/i,
        });
        const option = within(select).getByRole("option", { name: name });

        expect(option).toBeVisible();
        expect(option).toHaveValue(id);
      }
    );

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

    it("shows field for schema preview", () => {
      const previewPlaceholder = screen.getByText("Preview for your schema");
      expect(previewPlaceholder).toBeVisible();
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

  describe("shows errors when user does not fill out correctly", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedGetSchemaRegistryEnvironments
      );
      mockCreateSchemaRequest.mockImplementation(jest.fn());
      mockGetTopicNames.mockResolvedValue([testTopicName]);

      customRender(<TopicSchemaRequest topicName={testTopicName} />, {
        queryClient: true,
      });
      await waitForElementToBeRemoved(
        screen.getByTestId("environments-select-loading")
      );
    });

    afterEach(() => {
      cleanup();
    });

    // The new placeholder behaviour is to select the first option when no placeholder is provided
    // So this situation will never happen, as there always be a value selected in this context
    // @TODO https://github.com/aiven/klaw/issues/495
    // it("shows error when user does not fill out environment select", async () => {
    //   const form = getForm();
    //   const select = within(form).getByRole("combobox", {
    //     name: /Select environment/i,
    //   });

    //   select.focus();
    //   await userEvent.keyboard("{ArrowDown}");
    //   await userEvent.keyboard("{ESC}");
    //   await userEvent.tab();

    //   const error = await screen.findByText("The environment is required.");
    //   expect(error).toBeVisible();
    //   expect(select).toBeInvalid();
    // });

    it("shows error when user does not upload a file", async () => {
      const form = getForm();
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      fileInput.focus();
      await userEvent.tab();

      const fileRequiredError = screen.getAllByText("File is a required field");
      expect(fileRequiredError).toHaveLength(2);
    });

    it("does not enable button if user does not fill out all fields", async () => {
      const form = getForm();
      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const option = within(select).getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      const button = within(form).getByRole("button", {
        name: "Submit request",
      });
      expect(button).toBeDisabled();

      await userEvent.selectOptions(select, option);
      await userEvent.tab();

      expect(button).toBeDisabled();
    });
  });

  describe("enables user to cancel the form input", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedGetSchemaRegistryEnvironments
      );
      mockCreateSchemaRequest.mockImplementation(jest.fn());
      mockGetTopicNames.mockResolvedValue([testTopicName]);

      customRender(
        <TopicSchemaRequest
          topicName={testTopicName}
          schemafullValueForTest={"{}"}
        />,
        {
          queryClient: true,
        }
      );
      await waitForElementToBeRemoved(
        screen.getByTestId("environments-select-loading")
      );
    });

    afterEach(() => {
      cleanup();
    });

    it("redirects user to the previous page if the click 'Cancel'", async () => {
      const form = getForm();

      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const option = within(select).getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      const button = within(form).getByRole("button", {
        name: "Cancel",
      });

      await userEvent.selectOptions(select, option);
      await userEvent.tab();
      await userEvent.upload(fileInput, testFile);
      await userEvent.click(button);

      expect(mockedUsedNavigate).toHaveBeenCalledWith(-1);
    });
  });

  describe("handles errors from the api", () => {
    const originalConsoleError = console.error;

    beforeEach(async () => {
      console.error = jest.fn();
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedGetSchemaRegistryEnvironments
      );
      mockCreateSchemaRequest.mockRejectedValue({
        status: "400 BAD_REQUEST",
        data: { message: "Error in request" },
      });
      mockGetTopicNames.mockResolvedValue([testTopicName]);

      customRender(
        <TopicSchemaRequest
          topicName={testTopicName}
          schemafullValueForTest={"{}"}
        />,
        {
          queryClient: true,
        }
      );
      await waitForElementToBeRemoved(
        screen.getByTestId("environments-select-loading")
      );
    });

    afterEach(() => {
      console.error = originalConsoleError;
      cleanup();
    });

    it("shows an alert informing user about the error ", async () => {
      const form = getForm();

      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const option = within(select).getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      const button = within(form).getByRole("button", {
        name: "Submit request",
      });
      expect(button).toBeDisabled();

      await userEvent.selectOptions(select, option);
      await userEvent.tab();
      await userEvent.upload(fileInput, testFile);
      await userEvent.click(button);

      expect(mockCreateSchemaRequest).toHaveBeenCalled();

      const alert = await screen.findByRole("alert");
      const errorMessage = within(alert).getByText("Error in request");

      expect(errorMessage).toBeVisible();
      // it's not important that the console.error is called,
      // but it makes sure that 1) the console.error does not
      // show up in the test logs while 2) flagging an error
      // in case a console.error with a different message
      // gets called - which could be hinting to a problem
      expect(console.error).toHaveBeenCalledWith({
        status: "400 BAD_REQUEST",
        data: { message: "Error in request" },
      });
    });

    it("does not redirect the user if the request failed", async () => {
      const form = getForm();

      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const option = within(select).getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      const button = within(form).getByRole("button", {
        name: "Submit request",
      });
      expect(button).toBeDisabled();

      await userEvent.selectOptions(select, option);
      await userEvent.tab();
      await userEvent.upload(fileInput, testFile);
      await userEvent.click(button);

      await waitFor(() =>
        expect(screen.getByText("Error in request")).toBeVisible()
      );

      expect(mockedUsedNavigate).not.toHaveBeenCalled();

      expect(console.error).toHaveBeenCalledWith({
        status: "400 BAD_REQUEST",
        data: { message: "Error in request" },
      });
    });
  });

  describe("enables user to send a schema request", () => {
    beforeEach(async () => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue(
        mockedGetSchemaRegistryEnvironments
      );
      mockCreateSchemaRequest.mockResolvedValue({ status: "200 OK" });
      mockGetTopicNames.mockResolvedValue([testTopicName]);

      customRender(
        <TopicSchemaRequest
          topicName={testTopicName}
          schemafullValueForTest={"{}"}
        />,
        {
          queryClient: true,
        }
      );
      await waitForElementToBeRemoved(
        screen.getByTestId("environments-select-loading")
      );
    });

    afterEach(() => {
      cleanup();
    });

    it("enables user to select an environment", async () => {
      const form = getForm();
      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const option = within(select).getByRole("option", {
        name: mockedEnvironments[1].name,
      });

      // The new placeholder behaviour is to select the first option when no placeholder is provided
      // So this situation will never happen, as there always be a value selected in this context
      // @TODO https://github.com/aiven/klaw/issues/495
      expect(select).toHaveValue("1");

      await userEvent.selectOptions(select, option);

      expect(select).toHaveValue(mockedEnvironments[1].id);
      expect(select).toHaveDisplayValue(mockedEnvironments[1].name);
    });

    it("enables user to upload a file", async () => {
      const form = getForm();
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      expect(fileInput.files?.[0]).toBeUndefined();

      await userEvent.upload(fileInput, testFile);

      expect(fileInput.files?.[0]).toBe(testFile);
    });

    it("shows file content in a preview editor", async () => {
      const form = getForm();
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      await userEvent.upload(fileInput, testFile);
      const editor = screen.getByRole("textbox");

      await waitFor(() => expect(editor).toHaveDisplayValue(""));
    });

    it("enables the submit button if user filled all required inputs", async () => {
      const form = getForm();

      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const option = within(select).getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      const button = within(form).getByRole("button", {
        name: "Submit request",
      });
      expect(button).toBeDisabled();

      await userEvent.selectOptions(select, option);
      await userEvent.tab();
      await userEvent.upload(fileInput, testFile);

      expect(button).toBeEnabled();
    });

    it("submits on button click after user filled all required inputs", async () => {
      const form = getForm();

      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const option = within(select).getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      const button = within(form).getByRole("button", {
        name: "Submit request",
      });
      expect(button).toBeDisabled();

      await userEvent.selectOptions(select, option);
      await userEvent.tab();
      await userEvent.upload(fileInput, testFile);
      await userEvent.click(button);

      expect(mockCreateSchemaRequest).toHaveBeenCalledWith({
        environment: "1",
        remarks: "",
        schemafull: "{}",
        topicName: "my-awesome-topic",
      });
    });

    it("redirects user to '/mySchemaRequests after schema request was successful", async () => {
      const form = getForm();

      const select = within(form).getByRole("combobox", {
        name: /Select environment/i,
      });
      const option = within(select).getByRole("option", {
        name: mockedEnvironments[0].name,
      });
      const fileInput =
        within(form).getByLabelText<HTMLInputElement>(/Upload AVRO Schema/i);

      const button = within(form).getByRole("button", {
        name: "Submit request",
      });
      expect(button).toBeDisabled();

      await userEvent.selectOptions(select, option);
      await userEvent.tab();
      await userEvent.upload(fileInput, testFile);
      await userEvent.click(button);

      await waitFor(() =>
        expect(mockedUsedNavigate).toHaveBeenCalledWith(
          "/mySchemaRequests?reqsType=created&schemaCreated=true"
        )
      );
    });
  });
});
