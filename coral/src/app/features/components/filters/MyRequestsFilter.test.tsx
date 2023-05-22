import { customRender } from "src/services/test-utils/render-with-wrappers";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("MyRequestsFilter", () => {
  afterEach(() => {
    cleanup();
    window.history.replaceState(null, "", "/");
  });

  it("is checked if showOnlyMyRequests is true in the url search parameters", async () => {
    window.history.replaceState(null, "", "/?showOnlyMyRequests=true");
    customRender(<MyRequestsFilter />, {
      browserRouter: true,
    });
    await waitFor(() =>
      expect(window.location.search).toEqual("?showOnlyMyRequests=true")
    );
    await waitFor(() => {
      expect(
        screen.getByRole("checkbox", {
          name: "Show only my requests",
        })
      ).toBeChecked();
    });
  });

  it("is unchecked if showOnlyMyRequests in the url search parameters has other value than true", async () => {
    window.history.replaceState(null, "", "/?showOnlyMyRequests=abc");
    customRender(<MyRequestsFilter />, {
      browserRouter: true,
    });
    await waitFor(() =>
      expect(window.location.search).toEqual("?showOnlyMyRequests=abc")
    );
    await waitFor(() => {
      expect(
        screen.getByRole("checkbox", {
          name: "Show only my requests",
        })
      ).not.toBeChecked();
    });
  });

  it("sets the showOnlyMyRequests and page search parameter when user toggles the switch", async () => {
    window.history.replaceState(null, "", "/");
    customRender(<MyRequestsFilter />, {
      browserRouter: true,
    });
    await waitFor(() => expect(window.location.search).toEqual(""));
    const showMyRequests = screen.getByRole("checkbox", {
      name: "Show only my requests",
    });
    await userEvent.click(showMyRequests);
    await waitFor(() => expect(showMyRequests).toBeChecked());
    await waitFor(() =>
      expect(window.location.search).toEqual("?showOnlyMyRequests=true&page=1")
    );
  });

  it("unsets the showOnlyMyRequests and page search parameter when user untoggles the switch", async () => {
    window.history.replaceState(null, "", "/?showOnlyMyRequests=true");
    customRender(<MyRequestsFilter />, {
      browserRouter: true,
    });
    await waitFor(() =>
      expect(window.location.search).toEqual("?showOnlyMyRequests=true")
    );
    const showMyRequests = screen.getByRole("checkbox", {
      name: "Show only my requests",
    });
    await userEvent.click(showMyRequests);
    await waitFor(() => expect(showMyRequests).not.toBeChecked());
    await waitFor(() => expect(window.location.search).toEqual("?page=1"));
  });
});
