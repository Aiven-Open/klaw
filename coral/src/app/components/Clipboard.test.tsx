import { render, screen, cleanup, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import ClipBoard from "src/app/components/Clipboard";

describe("ClipBoard", () => {
  const mockCopyToClipboard = jest.fn();
  Object.defineProperty(global.navigator, "clipboard", {
    value: { writeText: mockCopyToClipboard },
    writable: true,
  });

  const props = {
    text: "Test text",
    accessibleCopyDescription: "Copy to clipboard",
    accessibleCopiedDescription: "Copied to clipboard",
    description: "Test description",
  };

  beforeEach(() => {
    render(<ClipBoard {...props} />);
  });

  afterEach(() => {
    cleanup();
    jest.clearAllMocks();
  });

  it("renders correctly", () => {
    const button = screen.getByRole("button", {
      name: props.accessibleCopyDescription,
    });
    expect(button).toBeVisible();
  });

  it("copies text to clipboard when button is clicked", async () => {
    const button = screen.getByRole("button", {
      name: props.accessibleCopyDescription,
    });
    await userEvent.click(button);
    await waitFor(() =>
      expect(mockCopyToClipboard).toHaveBeenCalledWith(props.text)
    );
  });

  it("renders feedback tooltip after button is clicked", async () => {
    const button = screen.getByRole("button", {
      name: props.accessibleCopyDescription,
    });
    await userEvent.click(button);
    await waitFor(() => expect(screen.getByText("Copied")).toBeVisible());
  });

  it("renders accessible feedback after button is clicked", async () => {
    const button = screen.getByRole("button", {
      name: props.accessibleCopyDescription,
    });
    await userEvent.click(button);
    await waitFor(() =>
      expect(
        screen.getByLabelText(props.accessibleCopiedDescription)
      ).toBeInTheDocument()
    );
  });
});
