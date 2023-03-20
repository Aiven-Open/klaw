import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import RequestsResourceTabs from "src/app/features/requests/RequestsResourceTabs";
import { RequestsTabEnum } from "src/app/router_utils";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

describe("RequestResourcesTabs", () => {
  let user: ReturnType<typeof userEvent.setup>;

  describe("Tab navigation", () => {
    beforeEach(() => {
      user = userEvent.setup();
      customRender(
        <RequestsResourceTabs currentTab={RequestsTabEnum.TOPICS} />,
        { memoryRouter: true }
      );
    });

    afterEach(() => {
      cleanup();
      mockedNavigate.mockReset();
    });

    it('navigates to correct URL when "ACLs" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "ACL requests" }));
      expect(mockedNavigate).toHaveBeenCalledWith("/requests/acls", {
        replace: true,
      });
    });

    it('navigates to correct URL when "Topics" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "Topic requests" }));
      expect(mockedNavigate).toHaveBeenCalledWith("/requests/topics", {
        replace: true,
      });
    });

    it('navigates to correct URL when "Schemas" tab is clicked', async () => {
      await user.click(screen.getByRole("tab", { name: "Schema requests" }));
      expect(mockedNavigate).toHaveBeenCalledWith("/requests/schemas", {
        replace: true,
      });
    });
    // Tab is disabled because Connectors are not yet implemented in coral
    // @TODO uncomment test when Connectors are implemented
    // it('navigates to correct URL when "Connectors" tab is clicked', async () => {
    //   await user.click(screen.getByRole("tab", { name: "Connectors" }));
    //   expect(mockedNavigate).toHaveBeenCalledWith("/requests/connectors", {
    //     replace: true,
    //   });
    // });
  });
});
