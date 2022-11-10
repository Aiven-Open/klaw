// @TODO import from "@testing-library/react/pure" disables auto cleanup, remove when main is merged
import { cleanup, render, screen, within } from "@testing-library/react/pure";
import { TopicCard } from "src/app/features/topics/list/TopicCard";

const testCard = {
  topicName: "aivtopic2",
  description: "Topic description",
  teamname: "Ospo",
  environmentsList: ["DEV"],
};

describe("TopicCard.tsx", () => {
  describe("renders a card with the required props", () => {
    const requiredProps = {
      topicName: testCard.topicName,
      description: testCard.description,
      teamname: testCard.teamname,
      environmentsList: testCard.environmentsList,
    };

    beforeAll(() => {
      render(<TopicCard {...requiredProps} />);
    });

    afterAll(cleanup);

    it("shows a list item ", () => {
      const listItem = screen.getByRole("listitem");

      expect(listItem).toBeVisible();
    });

    it("shows the topicName in the list item", () => {
      const headline = within(screen.getByRole("listitem")).getByRole(
        "heading",
        { name: requiredProps.topicName }
      );

      expect(headline).toBeVisible();
    });

    it("shows the topic description in the list item", () => {
      const headline = within(screen.getByRole("listitem")).getByText(
        requiredProps.description
      );

      expect(headline).toBeVisible();
    });

    it("shows the topic description in the list item", () => {
      const description = within(screen.getByRole("listitem")).getByText(
        requiredProps.description
      );

      expect(description).toBeVisible();
    });

    it("shows the term 'owner' in the list item", () => {
      const owner = within(screen.getByRole("listitem")).getByText("Owner");

      expect(owner).toBeVisible();
    });

    it("shows the teamname as owner in the list item", () => {
      const teamname = within(screen.getByRole("listitem")).getByText(
        requiredProps.teamname
      );

      expect(teamname).toHaveTextContent(requiredProps.teamname);
    });

    it("shows the term 'environments' in the list item", () => {
      const environments = within(screen.getByRole("listitem")).getByText(
        "Environments"
      );

      expect(environments).toBeVisible();
    });

    it("shows all items of the environmentsList in the list item", () => {
      requiredProps.environmentsList.forEach((env) => {
        const environment = within(screen.getByRole("listitem")).getByText(env);

        expect(environment).toBeVisible();
      });
    });

    it("shows a link get to the topic overview", () => {
      const accessibleLinkName = `Overview for topic ${requiredProps.topicName}`;

      const link = within(screen.getByRole("listitem")).getByRole("link", {
        name: accessibleLinkName,
      });

      expect(link).toBeEnabled();
    });
  });
});
