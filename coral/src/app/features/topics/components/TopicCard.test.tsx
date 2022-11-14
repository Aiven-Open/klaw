// @TODO import from "@testing-library/react/pure" disables auto cleanup, remove when main is merged
import { cleanup, render, screen } from "@testing-library/react/pure";
import { TopicCard } from "src/app/features/topics/components/TopicCard";

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

    it("shows the topicName in the topic's list item", () => {
      const headline = screen.getByRole("heading", {
        name: requiredProps.topicName,
      });

      expect(headline).toBeVisible();
    });

    it("shows the topic description in the topic's list item", () => {
      const description = screen.getByText(requiredProps.description);

      expect(description).toBeVisible();
    });

    it("shows the term 'owner' in the topic's list item", () => {
      const owner = screen.getByText("Owner");

      expect(owner).toBeVisible();
    });

    it("shows the all team names the topic's list item", () => {
      const teamname = screen.getByText(requiredProps.teamname);

      expect(teamname).toHaveTextContent(requiredProps.teamname);
    });

    it("shows the term 'environments' in the topic's list item", () => {
      const environments = screen.getByText("Environments");

      expect(environments).toBeVisible();
    });

    it("shows all environments in the topic's list item", () => {
      requiredProps.environmentsList.forEach((env) => {
        const environment = screen.getByText(env);

        expect(environment).toBeVisible();
      });
    });

    it("shows a link get to the topic overview", () => {
      const accessibleLinkName = `Overview for topic ${requiredProps.topicName}`;

      const link = screen.getByRole("link", {
        name: accessibleLinkName,
      });

      expect(link).toBeEnabled();
    });
  });
});
