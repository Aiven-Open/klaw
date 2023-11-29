import {
  cleanup,
  screen,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { TenantInformation } from "src/app/features/user-information/tenant-information/TenantInformation";
import { getMyTenantInfo } from "src/domain/user/user-api";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/user/user-api");
const mockGetMyTenantInfo = getMyTenantInfo as jest.MockedFunction<
  typeof getMyTenantInfo
>;

const mockGetMyTenantInfoResponseUser = {
  tenantName: "default",
  tenantDesc: "This is the default description",
  contactPerson: "Klaw Administrator",
  orgName: "Default Organization",
  authorizedToDelete: false,
  activeTenant: true,
};
const mockGetMyTenantInfoResponseSuperAdmin = {
  tenantName: "default",
  tenantDesc: "This is the default description",
  contactPerson: "Klaw Administrator",
  orgName: "Default Organization",
  authorizedToDelete: true,
  activeTenant: true,
};

describe("TenantInformation.tsx", () => {
  const originalConsoleError = console.error;

  beforeAll(() => {
    console.error = jest.fn();
  });

  afterAll(() => {
    console.error = originalConsoleError;
  });

  describe("renders tenant information data if user is not superadmin", () => {
    beforeEach(() => {
      mockGetMyTenantInfo.mockResolvedValue(mockGetMyTenantInfoResponseUser);
      customRender(<TenantInformation />, { queryClient: true });
    });

    afterEach(() => {
      cleanup();
    });

    it("renders static data", async () => {
      const skeletons = screen.getAllByRole("progressbar");
      await waitForElementToBeRemoved(skeletons);

      const labels = screen.getAllByRole("definition");
      const form = screen.queryByRole("form");

      expect(labels).toHaveLength(4);
      expect(form).toBeNull();
    });
  });

  describe("renders tenant information form if user is superadmin", () => {
    beforeEach(() => {
      mockGetMyTenantInfo.mockResolvedValue(
        mockGetMyTenantInfoResponseSuperAdmin
      );
      customRender(<TenantInformation />, { queryClient: true });
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("renders a form", async () => {
      const skeletons = screen.getAllByRole("progressbar");
      await waitForElementToBeRemoved(skeletons);

      const form = screen.getByRole("form");
      const labels = screen.queryAllByRole("definition");

      expect(form).toBeVisible();
      expect(labels).toHaveLength(0);
    });
  });

  describe("renders error if fetching the tenant info failed", () => {
    beforeEach(() => {
      mockGetMyTenantInfo.mockRejectedValue({
        success: false,
        message: "error",
      });
      customRender(<TenantInformation />, { queryClient: true });
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("renders an error", async () => {
      const skeletons = screen.getAllByRole("progressbar");
      await waitForElementToBeRemoved(skeletons);

      const errorBox = screen.getByRole("alert");

      expect(errorBox).toBeVisible();
      expect(errorBox).toHaveTextContent("error");
    });
  });
});
