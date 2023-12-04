import { cleanup, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import ApprovalResourceTabs from "src/app/features/approvals/components/ApprovalResourceTabs";
import { ApprovalsTabEnum } from "src/app/router_utils";
import { RequestsWaitingForApprovalWithTotal } from "src/domain/requests/requests-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const mockedPendingRequests: RequestsWaitingForApprovalWithTotal = {
  TOPIC: 1,
  ACL: 0,
  SCHEMA: 3,
  CONNECTOR: 2,
  USER: 2,
  OPERATIONAL: 0,
  TOTAL: 0,
};

jest.mock("src/app/context-provider/PendingRequestsProvider", () => ({
  usePendingRequestsContext: () => mockedPendingRequests,
}));

describe("ApprovalResourceTabs", () => {
  describe("Tab badges", () => {
    beforeAll(() => {
      customRender(
        <ApprovalResourceTabs currentTab={ApprovalsTabEnum.TOPICS} />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
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
  });
});

describe("Tab navigation", () => {
  beforeEach(() => {
    customRender(
      <ApprovalResourceTabs currentTab={ApprovalsTabEnum.TOPICS} />,
      { queryClient: true, memoryRouter: true }
    );
  });

  afterEach(() => {
    cleanup();
    mockedNavigate.mockReset();
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
