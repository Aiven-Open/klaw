import { cleanup, render, screen, within } from "@testing-library/react";
import TopicClaimBanner from "src/app/features/topics/details/components/TopicClaimBanner";
import userEvent from "@testing-library/user-event";

const mockSetShowClaimModal = jest.fn();

const testProps = {
  setShowClaimModal: mockSetShowClaimModal,
  isError: false,
};
describe("TopicClaimBanner", () => {
  afterEach(cleanup);

  it("renders correct elements", () => {
    render(<TopicClaimBanner {...testProps} />);
    const description = screen.getByText(
      "Your team is not the owner of this topic. Click below to create a claim request for this topic."
    );
    const button = screen.getByRole("button", { name: "Claim topic" });

    expect(description).toBeVisible();
    expect(button).toBeEnabled();
  });

  it("does not render error message when there is no error", () => {
    render(<TopicClaimBanner {...testProps} />);
    const error = screen.queryByRole("alert");

    expect(error).not.toBeInTheDocument();
  });

  it("renders error message when there is an error", () => {
    render(
      <TopicClaimBanner
        {...testProps}
        isError
        errorMessage={"There was an error"}
      />
    );
    const error = screen.getByRole("alert");
    const errorMessage = within(error).getByText("There was an error");

    expect(error).toBeVisible();
    expect(errorMessage).toBeVisible();
  });

  it("allows to start claim topic process", async () => {
    render(<TopicClaimBanner {...testProps} />);
    const button = screen.getByRole("button", { name: "Claim topic" });
    await userEvent.click(button);

    expect(mockSetShowClaimModal).toHaveBeenCalled();
  });
});
