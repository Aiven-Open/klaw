import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ApprovalResourceTabs from "src/app/features/approvals/components/ApprovalResourceTabs";
import { ApprovalsTabEnum } from "src/app/router_utils";
import * as requestApi from "src/domain/requests/requests-api";
import { RequestsWaitingForApproval } from "src/domain/requests/requests-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

class Deferred<T> {
  public promise: Promise<T>;
  public resolve!: (value: T | PromiseLike<T>) => void;
  public reject!: (value: T | PromiseLike<T>) => void;
  constructor() {
    this.promise = new Promise((resolve, reject) => {
      this.resolve = resolve;
      this.reject = reject;
    });
  }
}

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const mockedRequestsWaitingForApproval: RequestsWaitingForApproval = {
  TOPIC: 1,
  ACL: 0,
  SCHEMA: 3,
  CONNECTOR: 2,
  USER: 2,
};

describe("ApprovalResourceTabs", () => {
  let user: ReturnType<typeof userEvent.setup>;
  const getSpy = jest
    .spyOn(requestApi, "getRequestsWaitingForApproval")
    .mockImplementation(() => {
      throw Error("getNotificationCounts return must be mocked");
    });

  afterEach(() => {
    getSpy.mockReset();
  });

  describe("Tab badges", () => {
    let manual: Deferred<RequestsWaitingForApproval>;

    beforeAll(() => {
      manual = new Deferred();
      getSpy.mockReturnValue(manual.promise);
      customRender(
        <ApprovalResourceTabs currentTab={ApprovalsTabEnum.TOPICS} />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterAll(() => {
      cleanup();
    });
    describe("while is notification count request is in flight", () => {
      it("renders a tab for Topics", () => {
        screen.getByRole("tab", { name: "Topics" });
      });

      it("renders a tab for ACLs", () => {
        screen.getByRole("tab", { name: "ACLs" });
      });

      it("renders a tab for Schemas", () => {
        screen.getByRole("tab", { name: "Schemas" });
      });

      it("renders a tab for Connectors", () => {
        screen.getByRole("tab", { name: "Connectors" });
      });

      describe("when notification count request resolves", () => {
        beforeAll(() => {
          manual.resolve(mockedRequestsWaitingForApproval);
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
  });
  describe("Tab navigation", () => {
    beforeEach(() => {
      user = userEvent.setup();
      getSpy.mockResolvedValue(mockedRequestsWaitingForApproval);
      customRender(
        <ApprovalResourceTabs currentTab={ApprovalsTabEnum.TOPICS} />,
        { queryClient: true, memoryRouter: true }
      );
    });

    afterEach(() => {
      cleanup();
      mockedNavigate.mockReset();
    });

    it('navigates to correct URL when "ACLs" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "ACLs" }));
      expect(mockedNavigate).toHaveBeenCalledWith("/approvals/acls", {
        replace: true,
      });
    });

    it('navigates to correct URL when "Topics" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "Topics" }));
      expect(mockedNavigate).toHaveBeenCalledWith("/approvals/topics", {
        replace: true,
      });
    });

    it('navigates to correct URL when "Schemas" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "Schemas" }));
      expect(mockedNavigate).toHaveBeenCalledWith("/approvals/schemas", {
        replace: true,
      });
    });

    it('navigates to correct URL when "Connectors" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "Connectors" }));
      expect(mockedNavigate).toHaveBeenCalledWith("/approvals/connectors", {
        replace: true,
      });
    });
  });
});
