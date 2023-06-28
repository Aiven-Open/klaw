import { cleanup, render, screen } from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";

const mockSave = jest.fn();
const mockCancel = jest.fn();

const requiredProps = {
  save: mockSave,
  cancel: mockCancel,
};

describe("DocumentationEditor", () => {
  describe("shows all necessary elements when there is no existing documentation", () => {
    beforeAll(() => {
      render(<DocumentationEditor {...requiredProps} />);
    });

    afterAll(cleanup);

    it("shows a button group to switch between edit and preview", () => {
      const switchSection = screen.getByLabelText(
        "Switch between edit and preview mode"
      );
      expect(switchSection).toBeVisible();
    });

    it("shows a button for edit mode, marked as the active one", () => {
      const switchSection = screen.getByLabelText(
        "Switch between edit and preview mode"
      );

      const editMode = within(switchSection).getByRole("button", {
        name: "Edit",
        pressed: true,
      });
      expect(editMode).toBeEnabled();
    });

    it("shows a button for preview mode", () => {
      const switchSection = screen.getByLabelText(
        "Switch between edit and preview mode"
      );

      const previewMode = within(switchSection).getByRole("button", {
        name: "Preview",
      });
      expect(previewMode).toBeEnabled();
      expect(previewMode).toHaveAttribute("aria-pressed", "false");
    });

    it("shows a textarea", () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });

      expect(textarea).toBeEnabled();
    });

    it("shows the textarea as empty", () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });

      expect(textarea).toHaveValue("");
    });

    it("shows a description for the editor", () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });

      expect(textarea).toHaveAccessibleDescription(
        "We are supporting markdown following the CommonMark standard."
      );
    });

    it("applies specific style to textarea to hide text visually in favor of highlighted text", () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });

      // test does not really confirm the behaviour, but adds additional information about
      // the implementation which help with readability for tests
      expect(textarea).toHaveClass("markdownTextarea");
    });

    it("shows a element with syntax highlighted text for visual feedback", () => {
      const highlightedText = screen.getByTestId(
        "react-syntax-highlighter-mock"
      );

      expect(highlightedText).toBeVisible();
      expect(highlightedText.parentElement).toHaveAttribute(
        "aria-hidden",
        "true"
      );
    });

    it("shows a button to cancel the editor", () => {
      const cancel = screen.getByRole("button", { name: "Cancel" });

      expect(cancel).toBeEnabled();
    });

    it("shows a button to save the documentation", () => {
      const save = screen.getByRole("button", { name: "Save documentation" });

      expect(save).toBeEnabled();
    });
  });

  describe("shows all necessary elements including an existing documentation", () => {
    const testDocumentation = `# Hello world this is documentation`;
    beforeAll(() => {
      render(
        <DocumentationEditor
          {...requiredProps}
          documentation={testDocumentation}
        />
      );
    });

    afterAll(cleanup);

    it("shows a textarea", () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });

      expect(textarea).toBeEnabled();
    });

    it("shows the given documentation as value of textarea", () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });

      expect(textarea).toHaveValue(testDocumentation);
    });
  });
});
