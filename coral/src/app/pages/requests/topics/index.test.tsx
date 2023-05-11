import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, screen, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { getTopicRequests } from "src/domain/topic/topic-api";
import TopicRequestsPage from "src/app/pages/requests/topics/index";

vi.mock("src/domain/environment/environment-api.ts");
vi.mock("src/domain/topic/topic-api.ts");

const mockGetAllEnvironmentsForTopicAndAcl =
  getAllEnvironmentsForTopicAndAcl as vi.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;
const mockGetTopicRequests = getTopicRequests as vi.MockedFunction<
  typeof getTopicRequests
>;

describe("TopicRequestsPage", () => {
  beforeAll(async () => {
    mockGetAllEnvironmentsForTopicAndAcl.mockResolvedValue([]);
    mockGetTopicRequests.mockResolvedValue({
      entries: [],
      totalPages: 1,
      currentPage: 1,
    });

    customRender(<TopicRequestsPage />, {
      queryClient: true,
      memoryRouter: true,
    });
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
  });

  afterAll(cleanup);

  it("shows a preview banner to inform users about the early version of the view", () => {
    const previewBanner = screen.getByLabelText("Preview disclaimer");

    expect(previewBanner).toBeVisible();
    expect(previewBanner).toHaveTextContent(
      "You are viewing a preview of the redesigned user interface. You are one of our early reviewers, and your feedback will help us improve the product. You can always go back to the old interface."
    );
  });

  it("shows link back back to the original Klaw app for this view in the preview banner", () => {
    const previewBanner = screen.getByLabelText("Preview disclaimer");
    const link = within(previewBanner).getByRole("link", {
      name: "old interface",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute("href", "/myTopicRequests");
  });

  it("renders the topic request view", () => {
    const emptyRequests = screen.getByText(
      "No Topic request matched your criteria."
    );

    expect(emptyRequests).toBeVisible();
  });
});
