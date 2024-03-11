import { cleanup, screen, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
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
      customRender(
        <EnvironmentStatus
          envId="1"
          initialUpdateTime={""}
          initialEnvStatus="ONLINE"
        />,
        {
          queryClient: true,
        }
      );
      const text = screen.getByText("Online");
      const button = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      expect(text).toBeVisible();
      expect(button).toBeEnabled();
    });

    it("renders correct Status chip for OFFLINE and refresh button", () => {
      customRender(
        <EnvironmentStatus
          envId="1"
          initialUpdateTime={""}
          initialEnvStatus="OFFLINE"
        />,
        {
          queryClient: true,
        }
      );
      const text = screen.getByText("Offline");
      const button = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      expect(text).toBeVisible();
      expect(button).toBeEnabled();
    });

    it("renders correct Status chip for NOT_KNOWN and refresh button", () => {
      customRender(
        <EnvironmentStatus
          envId="1"
          initialUpdateTime={""}
          initialEnvStatus="NOT_KNOWN"
        />,
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
    const errorMessage = "Environment refresh error";

    beforeEach(() => {
      customRender(
        <EnvironmentStatus
          envId="1"
          initialUpdateTime={""}
          initialEnvStatus="ONLINE"
        />,
        {
          queryClient: true,
        }
      );
    });

    afterEach(() => {
      jest.clearAllMocks();

      cleanup();
    });

    it("renders correct Status chip when status does not change", async () => {
      mockGetUpdateEnvStatus.mockResolvedValue({
        result: "success",
        envStatus: "ONLINE",
        envStatusTime: "2023-09-21T11:47:15.664615239",
        envStatusTimeString: "21-Sep-2023 11:46:15",
      });

      expect(screen.getByText("Online")).toBeVisible();

      const refreshButton = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(screen.getByText("Online")).toBeVisible();
    });

    it("renders correct Status chip when status does change", async () => {
      mockGetUpdateEnvStatus.mockResolvedValue({
        result: "success",
        envStatus: "OFFLINE",
        envStatusTime: "2023-09-21T11:47:15.664615239",
        envStatusTimeString: "21-Sep-2023 11:46:15",
      });

      expect(screen.getByText("Online")).toBeVisible();

      const refreshButton = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(screen.getByText("Offline")).toBeVisible();
    });

    it("renders previous Status chip and toast when there is an error", async () => {
      jest.spyOn(console, "error").mockImplementationOnce((error) => error);

      mockGetUpdateEnvStatus.mockRejectedValue(errorMessage);

      expect(screen.getByText("Online")).toBeVisible();

      const refreshButton = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(screen.getByText("Online")).toBeVisible();

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
        envStatusTime: "2023-09-21T11:47:15.664615239",
        envStatusTimeString: "21-Sep-2023 11:46:15",
      });

      expect(screen.getByText("Online")).toBeVisible();

      const refreshButton = screen.getByRole("button", {
        name: "Refresh Environment status",
      });

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(screen.getByText("Offline")).toBeVisible();

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(screen.getByText("Offline")).toBeVisible();

      await userEvent.click(refreshButton);

      expect(mockGetUpdateEnvStatus).toHaveBeenCalledWith({ envId: "1" });
      expect(screen.getByText("Offline")).toBeVisible();
    });
  });
});
