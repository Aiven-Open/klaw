import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import EnvironmentStatus from "src/app/features/configuration/environments/components/EnvironmentStatus";
import { getUpdateEnvStatus } from "src/domain/environment";

import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

jest.mock("src/domain/environment/environment-api.ts");

const mockGetUpdateEnvStatus = getUpdateEnvStatus as jest.MockedFunction<
  typeof getUpdateEnvStatus
>;

describe("EnvironmentStatus", () => {
  describe("Status chips content", () => {
    afterEach(() => {
      cleanup();
    });

    it("renders correct Status chip for ONLINE and refresh button", () => {
      customRender(<EnvironmentStatus envId="1" initialEnvStatus="ONLINE" />, {
        queryClient: true,
      });
      const text = screen.getByText("Working");
      const button = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      expect(text).toBeVisible();
      expect(button).toBeEnabled();
    });

    it("renders correct Status chip for OFFLINE and refresh button", () => {
      customRender(<EnvironmentStatus envId="1" initialEnvStatus="OFFLINE" />, {
        queryClient: true,
      });
      const text = screen.getByText("Not working");
      const button = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      expect(text).toBeVisible();
      expect(button).toBeEnabled();
    });

    it("renders correct Status chip for NOT_KNOWN and refresh button", () => {
      customRender(
        <EnvironmentStatus envId="1" initialEnvStatus="NOT_KNOWN" />,
        {
          queryClient: true,
        }
      );
      const text = screen.getByText("Unknown");
      const button = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      expect(text).toBeVisible();
      expect(button).toBeEnabled();
    });
  });

  describe("Refresh status", () => {
    const originalConsoleError = console.error;
    const errorMessage = "Environment refresh error";

    beforeEach(() => {
      console.error = jest.fn();

      customRender(<EnvironmentStatus envId="1" initialEnvStatus="ONLINE" />, {
        queryClient: true,
      });
    });

    afterEach(() => {
      console.error = originalConsoleError;
      jest.clearAllMocks();

      cleanup();
    });

    it("renders correct Status chip when status does not change", async () => {
      mockGetUpdateEnvStatus.mockResolvedValue({
        result: "success",
        envStatus: "ONLINE",
      });

      expect(screen.getByText("Working")).toBeVisible();

      const refreshButton = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(console.error).not.toHaveBeenCalled();
      expect(screen.getByText("Working")).toBeVisible();
    });

    it("renders correct Status chip when status does change", async () => {
      mockGetUpdateEnvStatus.mockResolvedValue({
        result: "success",
        envStatus: "OFFLINE",
      });

      expect(screen.getByText("Working")).toBeVisible();

      const refreshButton = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(console.error).not.toHaveBeenCalled();
      expect(screen.getByText("Not working")).toBeVisible();
    });

    it("renders previous Status chip and toast when there is an error", async () => {
      mockGetUpdateEnvStatus.mockRejectedValue(errorMessage);

      expect(screen.getByText("Working")).toBeVisible();

      const refreshButton = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(screen.getByText("Working")).toBeVisible();

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith({
          message: `Could not refresh Environment status: Unexpected error`,
          position: "bottom-left",
          variant: "danger",
        })
      );

      expect(console.error).toHaveBeenCalledWith(errorMessage);
    });

    it("allows to refetch status several times in a row", async () => {
      mockGetUpdateEnvStatus.mockResolvedValue({
        result: "success",
        envStatus: "OFFLINE",
      });

      expect(screen.getByText("Working")).toBeVisible();

      const refreshButton = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(console.error).not.toHaveBeenCalled();
      expect(screen.getByText("Not working")).toBeVisible();

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(console.error).not.toHaveBeenCalled();
      expect(screen.getByText("Not working")).toBeVisible();

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(console.error).not.toHaveBeenCalled();
      expect(screen.getByText("Not working")).toBeVisible();
    });
  });
});
