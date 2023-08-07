import { cleanup, render, screen, within } from "@testing-library/react";
import TopicClaimBanner from "src/app/features/topics/details/components/TopicClaimBanner";
import userEvent from "@testing-library/user-event";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const TOPIC_NAME = "hello";

const mockSetShowClaimModal = jest.fn();

const testProps = {
  setShowClaimModal: mockSetShowClaimModal,
  isError: false,
  topicName: TOPIC_NAME,
  hasOpenClaimRequest: false,
  hasOpenRequest: false,
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

  it("renders correct state when there is already a claim request opened", async () => {
    customRender(
      <TopicClaimBanner {...testProps} hasOpenClaimRequest={true} />,
      { memoryRouter: true }
    );
    const description = screen.getByText(
      `There is already an open claim request for ${TOPIC_NAME}.`
    );
    const link = screen.getByRole("link", { name: "See the request" });

    expect(description).toBeVisible();
    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/requests/topics?search=${TOPIC_NAME}&requestType=CLAIM&status=CREATED&page=1`
    );
  });

  it("renders correct state when there is already a request opened by the owners of the topic", async () => {
    render(<TopicClaimBanner {...testProps} hasOpenRequest={true} />);
    const description = screen.getByText(
      `There is an open request for ${TOPIC_NAME} by the owners of this topic. Your team cannot claim ownership at this time.`
    );

    expect(description).toBeVisible();
  });
});
