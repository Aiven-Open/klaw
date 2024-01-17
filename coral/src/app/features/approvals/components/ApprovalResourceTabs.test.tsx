import { cleanup, screen, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { afterEach } from "node:test";
import ApprovalResourceTabs from "src/app/features/approvals/components/ApprovalResourceTabs";
import { ApprovalsTabEnum } from "src/app/router_utils";
import {
  RequestsWaitingForApprovalWithTotal,
  getRequestsWaitingForApproval,
} from "src/domain/requests";
import { KlawApiError } from "src/services/api";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

jest.mock("src/domain/requests/requests-api");

const mockGetRequestsWaitingForApproval =
  getRequestsWaitingForApproval as jest.MockedFunction<
    typeof getRequestsWaitingForApproval
  >;

const mockedPendingRequests: RequestsWaitingForApprovalWithTotal = {
  TOPIC: 1,
  ACL: 0,
  SCHEMA: 3,
  CONNECTOR: 2,
  USER: 2,
  OPERATIONAL: 0,
  TOTAL_NOTIFICATIONS: 8,
};

describe("ApprovalResourceTabs", () => {
  const originalConsoleError = console.error;

  beforeAll(() => {
    console.error = jest.fn();
  });

  afterAll(() => {
    console.error = originalConsoleError;
  });

  describe("Tab badges and navigation", () => {
    beforeAll(() => {
      mockGetRequestsWaitingForApproval.mockResolvedValue(
        mockedPendingRequests
      );

      customRender(
        <ApprovalResourceTabs currentTab={ApprovalsTabEnum.TOPICS} />,
        { queryClient: true, memoryRouter: true, aquariumContext: true }
      );
    });

    afterEach(() => {
      cleanup();
      jest.clearAllMocks();
    });

    it("renders correct pending approvals for Topics", async () => {
      await screen.findByRole("tab", {
        name: "Topics, 1 approval waiting",
      });
    });

    it("renders correct pending approvals for ACLs", async () => {
      await screen.findByRole("tab", {
        name: "ACLs, no pending approvals",
      });
    });

    it("renders correct pending approvals for Schemas", async () => {
      await screen.findByRole("tab", {
        name: "Schemas, 3 approvals waiting",
      });
    });

    it("renders correct pending approvals for Connectors", async () => {
      await screen.findByRole("tab", {
        name: "Connectors, 2 approvals waiting",
      });
    });

    it('navigates to correct URL when "Topics" tab is clicked', async () => {
      await userEvent.click(
        screen.getByRole("tab", { name: "Topics, 1 approval waiting" })
      );
      expect(mockedNavigate).toHaveBeenCalledWith("/approvals/topics", {
        replace: true,
      });
    });

    it('navigates to correct URL when "ACLs" tab is clicked', async () => {
      await userEvent.click(
        screen.getByRole("tab", { name: "ACLs, no pending approvals" })
      );
      expect(mockedNavigate).toHaveBeenCalledWith("/approvals/acls", {
        replace: true,
      });
    });

    it('navigates to correct URL when "Schemas" tab is clicked', async () => {
      await userEvent.click(
        screen.getByRole("tab", { name: "Schemas, 3 approvals waiting" })
      );
      expect(mockedNavigate).toHaveBeenCalledWith("/approvals/schemas", {
        replace: true,
      });
    });

    it('navigates to correct URL when "Connectors" tab is clicked', async () => {
      await userEvent.click(
        screen.getByRole("tab", { name: "Connectors, 2 approvals waiting" })
      );
      expect(mockedNavigate).toHaveBeenCalledWith("/approvals/connectors", {
        replace: true,
      });
    });
  });

  describe("shows an toast error notification when fetching pending requests fails", () => {
    const testError: KlawApiError = {
      message: "Oh no, this did not work",
      success: false,
    };

    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("calls useToast with correct error message", async () => {
      mockGetRequestsWaitingForApproval.mockRejectedValue(testError);

      customRender(
        <ApprovalResourceTabs currentTab={ApprovalsTabEnum.TOPICS} />,
        {
          memoryRouter: true,
          queryClient: true,
          aquariumContext: true,
        }
      );

      await waitFor(() =>
        expect(mockedUseToast).toHaveBeenCalledWith(
          expect.objectContaining({
            message: `Could not fetch pending requests: ${testError.message}`,
          })
        )
      );
    });
  });
});
