import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react/pure";
import {
  TopicSchema,
  readFile,
} from "src/app/features/topics/schema-request/components/TopicSchema";
import { renderForm } from "src/services/test-utils/render-form";
import { z } from "zod";

jest.mock("src/app/features/topics/schema-request/utils/read-file");
const mockReadFiles = readFile as jest.MockedFunction<typeof readFile>;

const formSchema = z.object({
  schema: z.string(),
});

describe("TopicSchema", () => {
  const mockedOnSubmit = jest.fn();
  const mockedOnError = jest.fn();

  describe("shows the schema preview skeleton", () => {
    beforeAll(() => {
      renderForm(
        <TopicSchema
          name={"schema"}
          file={undefined}
          formSchema={formSchema}
        />,
        {
          schema: formSchema,
          onSubmit: mockedOnSubmit,
          onError: mockedOnError,
        }
      );
    });

    afterAll(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a label like text", () => {
      const fakeLabel = screen.getByText("Schema preview (read only)");
      expect(fakeLabel).toBeVisible();
    });

    it("shows an information placeholder for preview", () => {
      const info = screen.getByText("Preview for your schema");
      expect(info).toBeVisible();
    });
  });

  describe("shows an editor with file content as preview", () => {
    const testFile: File = new File(
      ["{ test: 'value'}"],
      "my-awesome-schema.avsc",
      {
        type: "",
      }
    );
    beforeEach(async () => {
      mockReadFiles.mockResolvedValue('{ hello: "world" }');
      renderForm(
        <TopicSchema name={"schema"} file={testFile} formSchema={formSchema} />,
        {
          schema: formSchema,
          onSubmit: mockedOnSubmit,
          onError: mockedOnError,
        }
      );
      await waitForElementToBeRemoved(
        screen.getByText("Preview for your schema")
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("shows a editor as textarea", () => {
      const editor = screen.getByRole("textbox");
      expect(editor).toBeVisible();
    });

    it("reads the given file", () => {
      expect(mockReadFiles).toHaveBeenCalled();
    });
  });
});
