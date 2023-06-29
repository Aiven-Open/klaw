import { cleanup, render, screen } from "@testing-library/react";
import { within } from "@testing-library/react/pure";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";
import userEvent from "@testing-library/user-event";

const mockSave = jest.fn();
const mockCancel = jest.fn();

const requiredProps = {
  save: mockSave,
  cancel: mockCancel,
};

describe("DocumentationEditor", () => {
  const user = userEvent.setup();

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

  describe("enables user to switch between edit and preview mode", () => {
    beforeEach(() => {
      render(<DocumentationEditor {...requiredProps} />);
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("shows the edit mode selected per default", () => {
      const switchSection = screen.getByLabelText(
        "Switch between edit and preview mode"
      );

      const pressedButton = within(switchSection).getByRole("button", {
        pressed: true,
      });
      expect(pressedButton).toBeEnabled();
    });

    it("shows the textarea in edit mode", () => {
      const switchSection = screen.getByLabelText(
        "Switch between edit and preview mode"
      );

      const pressedButton = within(switchSection).getByRole("button", {
        pressed: true,
      });

      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });
      const previewView = screen.queryByTestId("react-markdown-mock");

      expect(pressedButton).toBeEnabled();
      expect(textarea).toBeVisible();
      expect(previewView).not.toBeInTheDocument();
    });

    it("enables user to switch to preview mode", async () => {
      const switchSection = screen.getByLabelText(
        "Switch between edit and preview mode"
      );
      const previewModeButton = within(switchSection).getByRole("button", {
        name: "Preview",
      });
      const editButton = within(switchSection).getByRole("button", {
        name: "Edit",
      });

      expect(previewModeButton).toHaveAttribute("aria-pressed", "false");
      expect(editButton).toHaveAttribute("aria-pressed", "true");

      await user.click(previewModeButton);

      const previewView = screen.getByTestId("react-markdown-mock");

      expect(previewModeButton).toHaveAttribute("aria-pressed", "true");
      expect(editButton).toHaveAttribute("aria-pressed", "false");
      expect(previewView).toBeVisible();
    });
  });

  describe("enables user to add documentation in markdown format", () => {
    const existingDoc = "#Hello";
    beforeEach(() => {
      render(
        <DocumentationEditor {...requiredProps} documentation={existingDoc} />
      );
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("allows user to write markdown in textarea", async () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });
      const highlightedText = screen.getByTestId(
        "react-syntax-highlighter-mock"
      );

      expect(textarea).toHaveValue(existingDoc);
      expect(highlightedText).toHaveTextContent(existingDoc);

      await user.click(textarea);
      await user.type(textarea, "{Enter}{Enter}## Documentation");

      expect(textarea).toHaveValue(`#Hello\n\n## Documentation`);
      // toHaveTextContent removes all no-text elements
      expect(highlightedText).toHaveTextContent(`#Hello ## Documentation`);
    });

    it("calls the given cancel function when user presses button", async () => {
      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      await user.click(cancelButton);

      expect(mockCancel).toHaveBeenCalled();
      expect(mockSave).not.toHaveBeenCalled();
    });

    it("does not call the given save function if user text did not change", async () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });
      const saveButton = screen.getByRole("button", {
        name: "Save documentation",
      });

      await user.type(textarea, "     ");

      await user.click(saveButton);

      expect(mockSave).not.toHaveBeenCalled();
    });

    it("calls the given save function when user clicks button", async () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });
      const saveButton = screen.getByRole("button", {
        name: "Save documentation",
      });

      await user.type(textarea, " world");

      await user.click(saveButton);

      expect(mockSave).toHaveBeenCalledWith("#Hello world");
    });
  });

  describe("enables usage with keyboard only", () => {
    beforeEach(() => {
      render(<DocumentationEditor {...requiredProps} />);
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("sets focus on textarea when user opens editor", async () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });

      expect(textarea).toHaveFocus();
    });

    it("enables the user to type without clicking textarea first", async () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });
      expect(textarea).toHaveValue("");

      await user.type(textarea, "#Hello world");
      expect(textarea).toHaveValue("#Hello world");
    });

    it("enables user to cancel input using keyboard only", async () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });
      const cancelButton = screen.getByRole("button", { name: "Cancel" });

      await user.type(textarea, " world");
      await user.tab();
      await user.tab();

      expect(cancelButton).toHaveFocus();

      await user.keyboard("{Enter}");

      expect(mockCancel).toHaveBeenCalled();
    });

    it("enables user to save input using keyboard only", async () => {
      const textarea = screen.getByRole("textbox", { name: "Markdown editor" });
      const saveButton = screen.getByRole("button", {
        name: "Save documentation",
      });

      await user.type(textarea, "#Hello world");
      await user.tab();
      await user.tab();
      await user.tab();

      expect(saveButton).toHaveFocus();

      await user.keyboard("{Enter}");

      expect(mockSave).toHaveBeenCalledWith("#Hello world");
    });

    it("enables user to switch to between edit and preview mode with keyboard only", async () => {
      const switchSection = screen.getByLabelText(
        "Switch between edit and preview mode"
      );
      const previewModeButton = within(switchSection).getByRole("button", {
        name: "Preview",
      });
      const editButton = within(switchSection).getByRole("button", {
        name: "Edit",
      });

      expect(previewModeButton).not.toHaveFocus();
      expect(editButton).not.toHaveFocus();

      expect(previewModeButton).toHaveAttribute("aria-pressed", "false");
      expect(editButton).toHaveAttribute("aria-pressed", "true");

      await user.tab({ shift: true });
      expect(previewModeButton).toHaveFocus();

      await user.tab({ shift: true });
      expect(editButton).toHaveFocus();

      await user.tab();
      expect(previewModeButton).toHaveFocus();
      await user.keyboard("{Enter}");

      const previewView = screen.getByTestId("react-markdown-mock");

      expect(previewModeButton).toHaveAttribute("aria-pressed", "true");
      expect(editButton).toHaveAttribute("aria-pressed", "false");
      expect(previewView).toBeVisible();
    });
  });
});
