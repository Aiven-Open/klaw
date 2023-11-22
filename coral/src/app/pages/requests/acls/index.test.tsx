import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, screen } from "@testing-library/react";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import AclRequestsPage from "src/app/pages/requests/acls/index";
import { getAclRequests } from "src/domain/acl/acl-api";

jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/acl/acl-api.ts");

const mockGetEnvironments =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;
const mockGetAclRequests = getAclRequests as jest.MockedFunction<
  typeof getAclRequests
>;

describe("AclRequestsPage", () => {
  beforeAll(async () => {
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

  afterAll(cleanup);

  it("renders the Acl request view", () => {
    const emptyRequests = screen.getByText(
      "No ACL request matched your criteria."
    );

    expect(emptyRequests).toBeVisible();
  });
});
