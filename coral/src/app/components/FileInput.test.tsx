import { cleanup, render, screen } from "@testing-library/react";
import { FileInput } from "src/app/components/FileInput";
import userEvent from "@testing-library/user-event";

const testValid = true;
const testLabelText = "Upload your favorite dog pic!";
const testButtonText = "Upload picture";
const testHelperText = "This is an error";
const testNoFileText = "No file chosen";
const testAccept = "image/jpeg";

const requiredProps = {
  valid: testValid,
  labelText: testLabelText,
  buttonText: testButtonText,
  helperText: testHelperText,
  noFileText: testNoFileText,
  accept: testAccept,
};

describe("FileInput.tsx", () => {
  describe("renders all necessary elements with required props", () => {
    beforeAll(() => {
      render(<FileInput {...requiredProps} />);
    });

    afterAll(cleanup);

    it("renders a file upload input", () => {
      // input type=file does not have a role to look for
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);

      expect(fileInput).toBeEnabled();
      expect(fileInput.tagName).toBe("INPUT");
    });

    it("marks the input as not required", () => {
      // input type=file does not have a role to look for
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);

      expect(fileInput).not.toBeRequired();
    });

    it("marks the input as not invalid when valid is true", () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);

      expect(fileInput).not.toBeInvalid();
    });

    it("shows a label looking text hidden from assistive technology", () => {
      const visualLabel = screen.getByTestId("file-input-fake-label");

      expect(visualLabel).toBeVisible();
      expect(visualLabel).toHaveTextContent(testLabelText);
      expect(visualLabel).toHaveAttribute("aria-hidden", "true");
    });

    it("shows a button looking element hidden from assistive technology", () => {
      const visualUploadElement = screen.getByTestId("file-input-fake-button");

      expect(visualUploadElement).toBeVisible();
      expect(visualUploadElement).toHaveTextContent(testButtonText);
      expect(visualUploadElement).toHaveAttribute("aria-hidden", "true");
    });

    it("shows an information for no filename available hidden from assistive technology", () => {
      const visualFileNameInfo = screen.getByTestId("file-input-filename-info");

      expect(visualFileNameInfo).toBeVisible();
      expect(visualFileNameInfo).toHaveTextContent(testNoFileText);
      expect(visualFileNameInfo).toHaveAttribute("aria-hidden", "true");
    });

    it("does not show an error message when valid is true", () => {
      const errorMessage = screen.queryByText(testHelperText);

      expect(errorMessage).not.toBeInTheDocument();
    });
  });

  describe("renders an invalid input with an error message when valid is false", () => {
    beforeAll(() => {
      render(<FileInput {...requiredProps} valid={false} />);
    });

    afterAll(cleanup);

    it("renders a file upload input", () => {
      // input type=file does not have a role to look for
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);

      expect(fileInput).toBeEnabled();
      expect(fileInput.tagName).toBe("INPUT");
    });

    it("marks the input as invalid", () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);

      expect(fileInput).toBeInvalid();
    });

    it("does show the error message", () => {
      const errorMessage = screen.getByText(testHelperText);

      expect(errorMessage).toBeVisible();
    });

    it("associates the error message with the input field", () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);

      expect(fileInput).toHaveAccessibleDescription(testHelperText);
    });
  });

  describe("renders a required input prop required is true", () => {
    beforeAll(() => {
      render(<FileInput {...requiredProps} required={true} />);
    });

    afterAll(cleanup);

    it("renders a file upload input", () => {
      // input type=file does not have a role to look for
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);

      expect(fileInput).toBeEnabled();
      expect(fileInput.tagName).toBe("INPUT");
    });

    it("marks the input as required", () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);

      expect(fileInput).toBeRequired();
    });

    it("shows fake label with * symbol as information for visual users", () => {
      const visualLabel = screen.getByTestId("file-input-fake-label");

      expect(visualLabel).toBeVisible();
      expect(visualLabel).toHaveTextContent(`${testLabelText}*`);
      expect(visualLabel).toHaveAttribute("aria-hidden", "true");
    });
  });

  describe("handles user uploading  files", () => {
    const fileName = "my-awesome-dog.jpeg";
    const testFile: File = new File(["I am a dog picture"], fileName, {
      type: "image/jpeg",
    });

    const onChangeMock = jest.fn();

    beforeEach(() => {
      render(<FileInput {...requiredProps} onChange={onChangeMock} />);
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("enables user to upload a file", async () => {
      const fileInput = screen.getByLabelText<HTMLInputElement>(testLabelText);
      await userEvent.upload(fileInput, testFile);

      expect(fileInput.files?.[0]).toBe(testFile);
      expect(onChangeMock).toHaveBeenCalledWith(
        expect.objectContaining({ target: fileInput })
      );
    });
  });
});
