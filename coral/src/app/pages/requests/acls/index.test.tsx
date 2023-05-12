import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, screen, within } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import AclRequestsPage from "src/app/pages/requests/acls/index";
import { getAclRequests } from "src/domain/acl/acl-api";

vi.mock("src/domain/environment/environment-api.ts");
vi.mock("src/domain/acl/acl-api.ts");

const mockGetEnvironments = vi.mocked(getAllEnvironmentsForTopicAndAcl);
const mockGetAclRequests = vi.mocked(getAclRequests);

describe("AclRequestsPage", () => {
  beforeEach(async () => {
    mockGetEnvironments.mockResolvedValue([]);
    mockGetAclRequests.mockResolvedValue({
      entries: [],
      totalPages: 1,
      currentPage: 1,
    });

    customRender(<AclRequestsPage />, {
      queryClient: true,
      memoryRouter: true,
    });
    await waitForElementToBeRemoved(screen.getByTestId("skeleton-table"));
  });

  afterEach(cleanup);

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
    expect(link).toHaveAttribute("href", "/myAclRequests");
  });

  it("renders the Acl request view", () => {
    const emptyRequests = screen.getByText(
      "No ACL request matched your criteria."
    );

    expect(emptyRequests).toBeVisible();
  });
});
