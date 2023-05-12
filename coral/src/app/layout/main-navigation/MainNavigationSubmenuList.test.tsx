import MainNavigationSubmenuList from "src/app/layout/main-navigation/MainNavigationSubmenuList";
import { cleanup, screen, render, within } from "@testing-library/react";
import data from "@aivenio/aquarium/dist/src/icons/console";
import userEvent from "@testing-library/user-event";

const textButtonSubmenuClosed = "Topics submenu, closed. Click to open.";
const textButtonSubmenuOpened = "Topics submenu, open. Click to close.";
const textSubmenuList = "Topics submenu";

const subNavigationLinks = [
  { name: "Submenu link one", href: "topcis-link-one" },
  { name: "Submenu link two", href: "topcis-link-two" },
  { name: "Submenu link three", href: "topcis-link-three" },
];

describe("MainNavigationSubmenuList.tsx", () => {
  // (icon is not needed for the test, Icon component mocked out)
  const mockIcon = "" as unknown as typeof data;

  const testMainNavigationLinks = subNavigationLinks.map((link, index) => {
    return (
      <a key={`${index}`} href={`${link.href}`}>
        {link.name}
      </a>
    );
  });

  describe('renders a collapsed submenu when "expanded" is not explicit set', () => {
    beforeEach(() => {
      render(
        <MainNavigationSubmenuList icon={mockIcon} text={"Topics"}>
          {testMainNavigationLinks}
        </MainNavigationSubmenuList>
      );
    });

    afterEach(cleanup);

    it(`renders a button with information about collapsed submenu for assistive technology`, () => {
      const button = screen.getByRole("button", {
        name: textButtonSubmenuClosed,
      });

      expect(button).toBeEnabled();
      expect(button).toHaveAttribute("aria-expanded", "false");
    });

    it(`hides the extended text for assistive technology visually`, () => {
      const button = screen.getByRole("button", {
        name: textButtonSubmenuClosed,
      });
      const text = within(button).getByText(textButtonSubmenuClosed);

      expect(text).toHaveClass("visually-hidden");
    });

    it(`shows two icons that are hidden from assistive technology`, () => {
      const button = screen.getByRole("button", {
        name: textButtonSubmenuClosed,
      });
      const icons = within(button).getAllByTestId("ds-icon");

      // DS Icons are all aria-hidden
      expect(icons).toHaveLength(2);
    });

    it(`shows a short text for button for visual users hidden for assistive technology`, () => {
      const button = screen.getByRole("button", {
        name: textButtonSubmenuClosed,
      });
      const text = within(button).getByText("Topics");
      const wrappingElement = text.parentNode;
      // this is a brittle test because it depends on a certain DOM structure
      // it's the only way right now to confirm that the text node in the
      // button is aria-hidden and assistive technology does not
      // announce it additionally to the visually-hidden text
      expect(wrappingElement).toHaveAttribute("aria-hidden", "true");
    });

    it(`renders no list with "Topics submenu" links`, () => {
      const list = screen.queryByRole("list", {
        name: textSubmenuList,
      });

      expect(list).not.toBeInTheDocument();
    });
  });

  describe('renders a expanded submenu when "expanded" is set to true', () => {
    beforeEach(() => {
      render(
        <MainNavigationSubmenuList
          icon={mockIcon}
          text={"Topics"}
          expanded={true}
        >
          {testMainNavigationLinks}
        </MainNavigationSubmenuList>
      );
    });

    afterEach(cleanup);

    it(`renders a button with information about expanded submenu for assistive technology`, () => {
      const button = screen.getByRole("button", {
        name: textButtonSubmenuOpened,
      });

      expect(button).toBeEnabled();
      expect(button).toHaveAttribute("aria-expanded", "true");
    });

    it(`hides the extended text for assistive technology visually`, () => {
      const button = screen.getByRole("button", {
        name: textButtonSubmenuOpened,
      });
      const text = within(button).getByText(textButtonSubmenuOpened);

      expect(text).toHaveClass("visually-hidden");
    });

    it(`shows two icons that are hidden from assistive technology`, () => {
      const button = screen.getByRole("button", {
        name: textButtonSubmenuOpened,
      });
      const icons = within(button).getAllByTestId("ds-icon");

      // DS Icons are all aria-hidden
      expect(icons).toHaveLength(2);
    });

    it(`shows a short text for button for visual users hidden for assistive technology`, () => {
      const button = screen.getByRole("button", {
        name: textButtonSubmenuOpened,
      });
      const text = within(button).getByText("Topics");
      const wrappingElement = text.parentNode;
      // this is a brittle test because it depends on a certain DOM structure
      // it's the only way right now to confirm that the text node in the
      // button is aria-hidden and assistive technology does not
      // announce it additionally to the visually-hidden text
      expect(wrappingElement).toHaveAttribute("aria-hidden", "true");
    });

    it(`renders a list with "Topics submenu" links`, () => {
      const list = screen.getByRole("list", {
        name: textSubmenuList,
      });

      expect(list).toBeVisible();
    });

    testMainNavigationLinks.forEach((item) => {
      const linkText = item.props.children;
      const href = item.props.href;

      it(`renders "${linkText}" link as list items of "Topics menu" list`, () => {
        const list = screen.getByRole("list", {
          name: textSubmenuList,
        });

        const link = within(list).getByRole("link", {
          name: linkText,
        });

        expect(link).toBeVisible();
        // this test is brittle because it depends on DOM structure
        // having the link be part of a listitem is important for
        // assistive technology though, so it should be confirmed
        expect((link.parentNode as HTMLElement).tagName).toBe("LI");
        expect(link).toHaveAttribute("href", href);
      });
    });
  });

  describe("enables user to open and close the submenu with mouse", () => {
    describe(`user can open a closed submenu`, () => {
      beforeEach(() => {
        render(
          <MainNavigationSubmenuList icon={mockIcon} text={"Topics"}>
            {testMainNavigationLinks}
          </MainNavigationSubmenuList>
        );
      });

      afterEach(cleanup);

      it(`updates button text for submenu item when user opens the menu`, async () => {
        const button = screen.getByRole("button");
        expect(button).toHaveTextContent(textButtonSubmenuClosed);

        await userEvent.click(button);
        expect(button).toHaveTextContent(textButtonSubmenuOpened);
      });

      it(`shows the submenu link list when user opens the menu`, async () => {
        const listHidden = screen.queryByRole("list");
        expect(listHidden).not.toBeInTheDocument();

        const button = screen.getByRole("button", {
          name: textButtonSubmenuClosed,
        });
        await userEvent.click(button);

        const list = screen.getByRole("list", { name: "Topics submenu" });
        expect(list).toBeVisible();
        expect(list?.parentNode).toHaveAttribute("aria-hidden", "false");
      });

      it(`shows the submenu links when user has opened the menu`, async () => {
        const linkOne = testMainNavigationLinks[0].props.children;
        const linkHidden = screen.queryByRole("link", {
          name: linkOne,
          hidden: true,
        });
        expect(linkHidden).not.toBeInTheDocument();

        const button = screen.getByRole("button", {
          name: textButtonSubmenuClosed,
        });
        expect(button).toHaveTextContent(textButtonSubmenuClosed);

        await userEvent.click(button);
        const link = screen.getByRole("link", { name: linkOne });
        expect(link).toBeVisible();
      });
    });

    describe(`user can close a open submenu`, () => {
      // case could have been covered with rendering
      // with "expanded" property, but that is a temp
      // implementation and the tests should be
      // future proofed
      beforeEach(async () => {
        render(
          <MainNavigationSubmenuList icon={mockIcon} text={"Topics"}>
            {testMainNavigationLinks}
          </MainNavigationSubmenuList>
        );
        const button = screen.getByRole("button");
        await userEvent.click(button);
        expect(button).toHaveTextContent(textButtonSubmenuOpened);
      });

      afterEach(cleanup);

      it(`updates button text for submenu item when user closes the menu`, async () => {
        const button = screen.getByRole("button");
        expect(button).toHaveTextContent(textButtonSubmenuOpened);

        await userEvent.click(button);
        expect(button).toHaveTextContent(textButtonSubmenuClosed);
      });

      it(`hides the submenu link list when user closes the menu`, async () => {
        const list = screen.getByRole("list", {
          name: textSubmenuList,
        });
        expect(list).toBeVisible();
        expect(list?.parentNode).toHaveAttribute("aria-hidden", "false");

        const button = screen.getByRole("button", {
          name: textButtonSubmenuOpened,
        });
        await userEvent.click(button);

        const listHidden = screen.queryByRole("list");
        expect(listHidden).not.toBeInTheDocument();
      });

      it(`hides the submenu links when user has closed the menu`, async () => {
        const linkOne = testMainNavigationLinks[0].props.children;
        const linkNotDisplayed = screen.getByRole("link", {
          name: linkOne,
        });
        expect(linkNotDisplayed).toBeVisible();

        const button = screen.getByRole("button", {
          name: textButtonSubmenuOpened,
        });
        await userEvent.click(button);
        const link = screen.queryByRole("link", {
          name: linkOne,
          hidden: true,
        });
        expect(link).not.toBeInTheDocument();
      });
    });
  });

  describe("enables user to navigate submenus with keyboard only", () => {
    describe(`user can open and close submenu`, () => {
      beforeEach(() => {
        render(
          <MainNavigationSubmenuList icon={mockIcon} text={"Topics"}>
            {testMainNavigationLinks}
          </MainNavigationSubmenuList>
        );
      });

      afterEach(cleanup);

      it(`opens the submenu when user presses "Enter"`, async () => {
        const listHidden = screen.queryByRole("list", {
          name: textSubmenuList,
        });
        expect(listHidden).not.toBeInTheDocument();

        const button = screen.getByRole("button", {
          name: textButtonSubmenuClosed,
        });
        button.focus();
        await userEvent.keyboard("{Enter}");

        const list = screen.getByRole("list", {
          name: textSubmenuList,
        });
        expect(list).toBeEnabled();
      });

      it(`closes the submenu when user presses "Enter" on a open menu`, async () => {
        const button = screen.getByRole("button", {
          name: textButtonSubmenuClosed,
        });
        button.focus();
        await userEvent.keyboard("{Enter}");

        const list = screen.getByRole("list", {
          name: textSubmenuList,
        });
        expect(list).toBeEnabled();

        await userEvent.keyboard("{Enter}");

        const listHidden = screen.queryByRole("list", {
          name: textSubmenuList,
        });
        expect(listHidden).not.toBeInTheDocument();
      });
    });

    describe(`user navigate through an open submenu`, () => {
      beforeEach(() => {
        render(
          <MainNavigationSubmenuList icon={mockIcon} text={"Topics"}>
            {testMainNavigationLinks}
          </MainNavigationSubmenuList>
        );
        const button = screen.getByRole("button", {
          name: textButtonSubmenuClosed,
        });
        button.focus();
      });

      afterEach(cleanup);

      const tabTrough = async (times: number) => {
        for (let i = times; i > 0; i--) {
          await userEvent.tab();
        }
      };

      subNavigationLinks.forEach((link, index) => {
        const numbersOfTabs = index + 1;
        const name = link.name;

        it(`focuses ${link.name} when user presses tab ${numbersOfTabs} times`, async () => {
          await userEvent.keyboard("{Enter}");
          await tabTrough(numbersOfTabs);
          const link = screen.getByRole("link", { name });

          expect(link).toHaveFocus();
        });
      });
    });

    describe(`user can navigate backward through an open submenu`, () => {
      beforeEach(async () => {
        render(
          <MainNavigationSubmenuList icon={mockIcon} text={"Topics"}>
            {testMainNavigationLinks}
          </MainNavigationSubmenuList>
        );
        const button = screen.getByRole("button", {
          name: textButtonSubmenuClosed,
        });
        button.focus();
        await userEvent.keyboard("{Enter}");

        const lastElement =
          subNavigationLinks[subNavigationLinks.length - 1].name;
        const lastNavItem = screen.getByRole("link", {
          name: lastElement,
        });
        lastNavItem.focus();
      });

      afterEach(cleanup);

      const tabBackTrough = async (times: number) => {
        for (let i = times; i > 0; i--) {
          await userEvent.tab({ shift: true });
        }
      };

      const submenuReversed = [...subNavigationLinks].reverse();
      submenuReversed.forEach((link, index) => {
        const numbersOfTabs = index;
        const name = link.name;

        it(`focuses ${link.name} when user presses tab ${numbersOfTabs} times`, async () => {
          await tabBackTrough(numbersOfTabs);
          const link = screen.getByRole("link", { name });

          expect(link).toHaveFocus();
          expect(true).toBeTruthy();
        });
      });
    });
  });
});
