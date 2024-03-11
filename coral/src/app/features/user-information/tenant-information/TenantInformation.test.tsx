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

describe("TenantInformation.tsx", () => {
  describe("renders tenant information data", () => {
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

  describe("renders error if fetching the tenant info failed", () => {
    beforeEach(() => {
      jest.spyOn(console, "error").mockImplementation((error) => error);
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
      expect(console.error).toHaveBeenCalledWith({
        message: "error",
        success: false,
      });
    });
  });
});
