import { cleanup, screen } from "@testing-library/react/pure";
import userEvent from "@testing-library/user-event";
import {
  TopicSchema,
  readFile,
} from "src/app/features/topics/schema-request/components/TopicSchema";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";
import { TopicRequestFormSchema } from "src/app/features/topics/schema-request/utils/zod-schema";

jest.mock("src/app/features/topics/schema-request/utils/read-file");
const mockReadFiles = readFile as jest.MockedFunction<typeof readFile>;

// Temp mock for the editor, based on this:
// https://github.com/suren-atoyan/monaco-react/issues/88#issuecomment-887055307
// github issue to investigate a reusable testing approach:
// 🐙 https://github.com/aiven/klaw/issues/475
jest.mock("@monaco-editor/react", () => {
  return jest.fn((props) => {
    return (
      <textarea
        value={props.value}
        onChange={(e) => props.onChange(e.target.value)}
      ></textarea>
    );
  });
});

describe("TopicSchema", () => {
  const formElementName: keyof TopicRequestFormSchema = "schemafull";
  const formSchema = z.object({
    schemafull: z.instanceof(File),
  });

  const mockedOnSubmit = jest.fn();
  const mockedOnError = jest.fn();
  const labelUpload = "Upload AVRO schema file";
  const emptyError = "File is a required field";

  const testFile: File = new File(
    ["{ test: 'value'}"],
    "my-awesome-schema.avsc",
    {
      type: "",
    }
  );

  describe("renders all necessary elements", () => {
    beforeAll(() => {
      renderForm(<TopicSchema name={formElementName} required={true} />, {
        schema: formSchema,
        onSubmit: mockedOnSubmit,
        onError: mockedOnError,
      });
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a required file upload input", () => {
      // input type=file does not have a role to look for
      const fileInput = screen.getByLabelText<HTMLInputElement>(labelUpload);

      expect(fileInput).toBeEnabled();
      expect(fileInput).toBeRequired();
      expect(fileInput.tagName).toBe("INPUT");
    });

    it("shows a label like text for the preview", () => {
      const fakeLabel = screen.getByText("Schema preview (read only)");
      expect(fakeLabel).toBeVisible();
    });

    it("shows an information placeholder for the preview", () => {
      const info = screen.getByText("Preview for your schema");
      expect(info).toBeVisible();
    });

    it("renders the hidden editor for the preview", () => {
      const editor = screen.getByRole("textbox", {
        hidden: true,
      });
      expect(editor).toBeInTheDocument();
      expect(editor).not.toBeVisible();
    });
  });

  describe("informs user about errors", () => {
    beforeEach(() => {
      renderForm(<TopicSchema name={formElementName} required={true} />, {
        schema: formSchema,
        onSubmit: mockedOnSubmit,
        onError: mockedOnError,
      });
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("marks the input as invalid if user accesses and leave the input without uploading", async () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(labelUpload);

      //@TODO check this again / sync with team
      // this assert fails and I can't figure out why!
      // screen output: <input aria-invalid="false"
      // it's valid in the real DOM, same test for
      // FileInput works, setting the value hard to true
      // fails, removing it altogether fails -? ?((
      // expect(fileInput).toBeValid();
      fileInput.focus();
      await userEvent.tab();

      expect(fileInput).toBeInvalid();
    });

    it("shows an error if user accesses and leave the input without uploading", async () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(labelUpload);

      expect(screen.queryByText(emptyError)).not.toBeInTheDocument();
      fileInput.focus();
      await userEvent.tab();

      const errors = screen.getAllByText(emptyError);
      expect(errors).toHaveLength(2);

      expect(errors[0]).toBeVisible();
      expect(errors[1]).toHaveAttribute("aria-hidden");
    });
  });

  describe("enables user to upload a schema file", () => {
    const testSchema = JSON.stringify({ doc: "schematest" });

    beforeEach(() => {
      mockReadFiles.mockResolvedValue(testSchema);

      renderForm(<TopicSchema name={formElementName} required={true} />, {
        schema: formSchema,
        onSubmit: mockedOnSubmit,
        onError: mockedOnError,
      });
    });

    afterEach(() => {
      jest.clearAllMocks();
      cleanup();
    });

    it("processes the file when user uploaded it", async () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(labelUpload);

      await userEvent.upload(fileInput, testFile);

      expect(mockReadFiles).toHaveBeenCalled();
    });

    it("shows an accessible information about the uploaded file", async () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(labelUpload);
      await userEvent.upload(fileInput, testFile);

      const successInfo = screen.getByText(
        "Uploaded file, name: my-awesome-schema.avsc. Click to upload new file."
      );
      expect(successInfo).toBeVisible();
    });

    it("shows a readonly editor with file content as preview ", async () => {
      const editor = screen.getByRole<HTMLTextAreaElement>("textbox", {
        hidden: true,
      });

      expect(editor).toHaveDisplayValue("");
      const fileInput = screen.getByLabelText<HTMLInputElement>(labelUpload);
      await userEvent.upload(fileInput, testFile);

      expect(editor).toBeVisible();
      expect(editor).toHaveDisplayValue(testSchema);
    });
  });

  // to test this and the validation, we need a better
  // way to test the monaco editor (see comment and
  // link to issue above
  // alternative: test in browse environment
  // Will leave this in as reminder
  test.todo("submits the form if input is valid");
  test.todo("validates content of the uploaded file");
});
