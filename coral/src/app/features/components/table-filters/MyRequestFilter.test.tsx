import { customRender } from "src/services/test-utils/render-with-wrappers";
import { MyRequestFilter } from "src/app/features/components/table-filters/MyRequestFilter";
import { cleanup, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("MyRequestFilter", () => {
  afterEach(() => {
    cleanup();
    window.history.replaceState(null, "", "/");
  });

  it("is checked if isMyRequest is true in the url search parameters", async () => {
    window.history.replaceState(null, "", "/?isMyRequest=true");
    customRender(<MyRequestFilter />, {
      browserRouter: true,
    });
    await waitFor(() =>
      expect(window.location.search).toEqual("?isMyRequest=true")
    );
    await waitFor(() => {
      expect(
        screen.getByRole("checkbox", {
          name: "Show My Requests Only show my requests",
        })
      ).toBeChecked();
    });
  });

  it("is unchecked if isMyRequest in the url search parameters has other value than true", async () => {
    window.history.replaceState(null, "", "/?isMyRequest=abc");
    customRender(<MyRequestFilter />, {
      browserRouter: true,
    });
    await waitFor(() =>
      expect(window.location.search).toEqual("?isMyRequest=abc")
    );
    await waitFor(() => {
      expect(
        screen.getByRole("checkbox", {
          name: "Show My Requests Only show my requests",
        })
      ).not.toBeChecked();
    });
  });

  it("sets the isMyRequest and page search parameter when user toggles the switch", async () => {
    window.history.replaceState(null, "", "/");
    customRender(<MyRequestFilter />, {
      browserRouter: true,
    });
    await waitFor(() => expect(window.location.search).toEqual(""));
    const showMyRequests = screen.getByRole("checkbox", {
      name: "Show My Requests Only show my requests",
    });
    await userEvent.click(showMyRequests);
    await waitFor(() => expect(showMyRequests).toBeChecked());
    await waitFor(() =>
      expect(window.location.search).toEqual("?isMyRequest=true&page=1")
    );
  });

  it("unsets the isMyRequest and page search parameter when user untoggles the switch", async () => {
    window.history.replaceState(null, "", "/?isMyRequest=true");
    customRender(<MyRequestFilter />, {
      browserRouter: true,
    });
    await waitFor(() =>
      expect(window.location.search).toEqual("?isMyRequest=true")
    );
    const showMyRequests = screen.getByRole("checkbox", {
      name: "Show My Requests Only show my requests",
    });
    await userEvent.click(showMyRequests);
    await waitFor(() => expect(showMyRequests).not.toBeChecked());
    await waitFor(() => expect(window.location.search).toEqual("?page=1"));
  });
});
