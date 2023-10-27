import { cleanup, screen, within } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { BreadCrumbsWithLinks } from "src/app/pages/BreadCrumbsWithLinks";
import { customRender } from "src/services/test-utils/render-with-wrappers";

describe("BreadCrumbsWithLinks", () => {
  beforeEach(() =>
    customRender(<BreadCrumbsWithLinks paths={["hello", "there"]} />, {
      browserRouter: true,
    })
  );

  afterEach(cleanup);

  it("shows breadcrumbs to the provided paths", async () => {
    const breadcrumbs = screen.getByRole("navigation", {
      name: "Breadcrumbs",
    });
    const linkBreadcrumb = within(breadcrumbs).getByRole("link", {
      name: "hello",
    });
    const inactiveLinkBreadcrumb = within(breadcrumbs).queryByRole("link", {
      name: "there",
    });

    expect(linkBreadcrumb).toBeVisible();
    expect(inactiveLinkBreadcrumb).toBeVisible();
  });

  it("shows inactive link for last element of breadcrumbs", async () => {
    const breadcrumbs = screen.getByRole("navigation", {
      name: "Breadcrumbs",
    });

    const inactiveLinkBreadcrumb = within(breadcrumbs).getByRole("link", {
      name: "there",
    });

    await userEvent.click(inactiveLinkBreadcrumb);

    expect(window.location.pathname).toBe("/");
  });

  it("shows active link for first elements of breadcrumbs", async () => {
    const breadcrumbs = screen.getByRole("navigation", {
      name: "Breadcrumbs",
    });

    const linkBreadcrumb = within(breadcrumbs).getByRole("link", {
      name: "hello",
    });

    expect(linkBreadcrumb).toBeEnabled();

    await userEvent.click(linkBreadcrumb);

    expect(window.location.pathname).toBe("/hello");
  });
});
