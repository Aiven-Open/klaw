import { cleanup, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { RequestsDropdown } from "src/app/layout/header/RequestsDropdown";
import { getRequestsWaitingForApproval } from "src/domain/requests";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const noRequests = {
  TOPIC: 0,
  ACL: 0,
  SCHEMA: 0,
  CONNECTOR: 0,
  OPERATIONAL: 0,
  USER: 0,
  TOTAL_NOTIFICATIONS: 0,
};

const someRequests = {
  TOPIC: 1,
  ACL: 1,
  SCHEMA: 1,
  CONNECTOR: 1,
  OPERATIONAL: 1,
  USER: 1,
  TOTAL_NOTIFICATIONS: 4,
};

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

jest.mock("src/domain/requests/requests-api");

const mockGetRequestsWaitingForApproval =
  getRequestsWaitingForApproval as jest.MockedFunction<
    typeof getRequestsWaitingForApproval
  >;

describe("RequestsDropdown", () => {
  const user = userEvent.setup();

  describe("has no pending requests", () => {
    beforeAll(() => {
      mockGetRequestsWaitingForApproval.mockResolvedValue(noRequests);
    });

    afterAll(() => {
      jest.clearAllMocks();
    });

    describe("renders all necessary elements when dropdown is closed", () => {
      beforeAll(() => {
        customRender(<RequestsDropdown />, {
          memoryRouter: true,
          queryClient: true,
        });
      });
      afterAll(cleanup);

      it("shows a button to open menu", () => {
        const button = screen.getByRole("button", {
          name: "No pending requests",
        });
        expect(button).toBeEnabled();
      });

      it("shows popup information on the button to support assistive technology", () => {
        const button = screen.getByRole("button", {
          name: "No pending requests",
        });

        expect(button).toHaveAttribute("aria-haspopup", "true");
      });

      it("shows that menu is not expanded on the button to support assistive technology", () => {
        const button = screen.getByRole("button", {
          name: "No pending requests",
        });

        expect(button).toHaveAttribute("aria-expanded", "false");
      });
    });

    describe("renders all necessary elements when dropdown is open", () => {
      beforeAll(async () => {
        customRender(<RequestsDropdown />, {
          memoryRouter: true,
          queryClient: true,
        });
        const button = screen.getByRole("button", {
          name: "No pending requests",
        });
        await user.click(button);
      });

      afterAll(cleanup);

      it("shows topic requests", () => {
        const menuItem = screen.getByRole("menuitem", {
          name: "No pending topic requests",
        });

        expect(menuItem).toBeVisible();
      });

      it("shows ACL requests", () => {
        const menuItem = screen.getByRole("menuitem", {
          name: "No pending ACL requests",
        });

        expect(menuItem).toBeVisible();
      });

      it("shows schema requests ", () => {
        const menuItem = screen.getByRole("menuitem", {
          name: "No pending schema requests",
        });

        expect(menuItem).toBeVisible();
      });

      it("shows Kafka connector requests", () => {
        const menuItem = screen.getByRole("menuitem", {
          name: "No pending Kafka connector requests",
        });

        expect(menuItem).toBeVisible();
      });
    });

    describe("handles user choosing items from the menu", () => {
      beforeEach(() => {
        Object.defineProperty(window, "location", {
          value: {
            assign: jest.fn(),
          },
          writable: true,
        });
        customRender(<RequestsDropdown />, {
          memoryRouter: true,
          queryClient: true,
        });
      });

      afterEach(() => {
        jest.restoreAllMocks();
        cleanup();
      });

      it("navigates to topic requests table", async () => {
        const button = screen.getByRole("button", {
          name: "No pending requests",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", {
          name: "No pending topic requests",
        });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith("/approvals/topics");
      });

      it("navigates to ACL requests table", async () => {
        const button = screen.getByRole("button", {
          name: "No pending requests",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", {
          name: "No pending ACL requests",
        });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith("/approvals/acls");
      });

      it("navigates to schema requests table", async () => {
        const button = screen.getByRole("button", {
          name: "No pending requests",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", {
          name: "No pending schema requests",
        });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith("/approvals/schemas");
      });

      it("navigates to Kafka connector requests table", async () => {
        const button = screen.getByRole("button", {
          name: "No pending requests",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", {
          name: "No pending Kafka connector requests",
        });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith("/approvals/connectors");
      });
    });
  });

  describe("has some pending requests", () => {
    beforeAll(() => {
      mockGetRequestsWaitingForApproval.mockResolvedValue(someRequests);
    });

    afterAll(() => {
      jest.clearAllMocks();
    });

    describe("renders all necessary elements when dropdown is closed", () => {
      beforeAll(async () => {
        customRender(<RequestsDropdown />, {
          memoryRouter: true,
          queryClient: true,
        });
      });

      afterAll(cleanup);

      it("shows a button to open menu", async () => {
        const button = await screen.findByRole("button", {
          name: "See 4 pending requests",
        });
        expect(button).toBeEnabled();
      });

      it("shows popup information on the button to support assistive technology", async () => {
        const button = await screen.findByRole("button", {
          name: "See 4 pending requests",
        });

        expect(button).toHaveAttribute("aria-haspopup", "true");
      });

      it("shows that menu is not expanded on the button to support assistive technology", async () => {
        const button = await screen.findByRole("button", {
          name: "See 4 pending requests",
        });

        expect(button).toHaveAttribute("aria-expanded", "false");
      });
    });

    describe("renders all necessary elements when dropdown is open", () => {
      beforeAll(async () => {
        customRender(<RequestsDropdown />, {
          memoryRouter: true,
          queryClient: true,
        });
        const button = await screen.findByRole("button", {
          name: "See 4 pending requests",
        });
        await user.click(button);
      });

      afterAll(cleanup);

      it("shows topic requests", () => {
        const menuItem = screen.getByRole("menuitem", {
          name: "1 pending topic requests",
        });

        expect(menuItem).toBeVisible();
      });

      it("shows ACL requests", () => {
        const menuItem = screen.getByRole("menuitem", {
          name: "1 pending ACL requests",
        });

        expect(menuItem).toBeVisible();
      });

      it("shows schema requests ", () => {
        const menuItem = screen.getByRole("menuitem", {
          name: "1 pending schema requests",
        });

        expect(menuItem).toBeVisible();
      });

      it("shows Kafka connector requests", () => {
        const menuItem = screen.getByRole("menuitem", {
          name: "1 pending Kafka connector requests",
        });

        expect(menuItem).toBeVisible();
      });
    });

    describe("handles user choosing items from the menu", () => {
      beforeEach(() => {
        Object.defineProperty(window, "location", {
          value: {
            assign: jest.fn(),
          },
          writable: true,
        });
        customRender(<RequestsDropdown />, {
          memoryRouter: true,
          queryClient: true,
        });
      });

      afterEach(() => {
        jest.restoreAllMocks();
        cleanup();
      });

      it("navigates to topic requests table", async () => {
        const button = await screen.findByRole("button", {
          name: "See 4 pending requests",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", {
          name: "1 pending topic requests",
        });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith("/approvals/topics");
      });

      it("navigates to ACL requests table", async () => {
        const button = await screen.findByRole("button", {
          name: "See 4 pending requests",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", {
          name: "1 pending ACL requests",
        });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith("/approvals/acls");
      });

      it("navigates to schema requests table", async () => {
        const button = await screen.findByRole("button", {
          name: "See 4 pending requests",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", {
          name: "1 pending schema requests",
        });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith("/approvals/schemas");
      });

      it("navigates to Kafka connector requests table", async () => {
        const button = await screen.findByRole("button", {
          name: "See 4 pending requests",
        });
        await user.click(button);

        const menuItem = screen.getByRole("menuitem", {
          name: "1 pending Kafka connector requests",
        });
        await user.click(menuItem);

        expect(mockedNavigate).toHaveBeenCalledWith("/approvals/connectors");
      });
    });
  });
});
