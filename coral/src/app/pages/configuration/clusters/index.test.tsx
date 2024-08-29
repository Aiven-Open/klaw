import { ClustersPage } from "src/app/pages/configuration/clusters";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { cleanup, screen } from "@testing-library/react";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";

jest.mock("src/app/context-provider/AuthProvider");

const mockUseAuthContext = useAuthContext as jest.MockedFunction<
  typeof useAuthContext
>;

describe("ClustersPage", () => {
  afterEach(() => {
    jest.resetAllMocks();
    cleanup();
  });

  it('renders "Add new cluster" button when addDeleteEditClusters permission is true', () => {
    mockUseAuthContext.mockReturnValue({
      ...testAuthUser,
      isSuperAdminUser: false,
      permissions: { ...testAuthUser.permissions, addDeleteEditClusters: true },
    });

    customRender(<ClustersPage />, {
      memoryRouter: true,
      queryClient: true,
      aquariumContext: true,
    });

    const button = screen.getByRole("button", { name: "Add new cluster" });
    expect(button).toBeVisible();
  });

  it('does not render "Add new cluster" button when addDeleteEditClusters permission is false', () => {
    mockUseAuthContext.mockReturnValue({
      ...testAuthUser,
      isSuperAdminUser: false,
      permissions: {
        ...testAuthUser.permissions,
        addDeleteEditClusters: false,
      },
    });

    customRender(<ClustersPage />, {
      memoryRouter: true,
      queryClient: true,
      aquariumContext: true,
    });

    const button = screen.queryByRole("button", { name: "Add new cluster" });
    expect(button).toBeNull();
  });
});
