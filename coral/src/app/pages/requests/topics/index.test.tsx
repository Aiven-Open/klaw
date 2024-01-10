import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, screen } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { getTopicRequests } from "src/domain/topic/topic-api";
import TopicRequestsPage from "src/app/pages/requests/topics/index";

jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/topic/topic-api.ts");

const mockGetAllEnvironmentsForTopicAndAcl =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;
const mockGetTopicRequests = getTopicRequests as jest.MockedFunction<
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
      aquariumContext: true,
    });
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
  });

  afterAll(cleanup);

  it("renders the topic request view", () => {
    const emptyRequests = screen.getByText(
      "No Topic request matched your criteria."
    );

    expect(emptyRequests).toBeVisible();
  });
});
