import { KlawApiModel } from "types/utils";
import { cleanup, render, screen } from "@testing-library/react";
import { PromotionBanner } from "src/app/features/topics/details/components/PromotionBanner";
import userEvent from "@testing-library/user-event";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const promotionDetails: KlawApiModel<"PromotionStatus"> = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
};
const testTopicName = "my-test-topic";

describe("PromotionBanner", () => {
  const user = userEvent.setup();

  describe("does not show a promotion banner at all dependent on promotion details", () => {
    afterEach(cleanup);

    it("returns null if status is NOT_AUTHORIZED", () => {
      const { container } = render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "NOT_AUTHORIZED" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if status is NO_PROMOTION", () => {
      const { container } = render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "NO_PROMOTION" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if status is FAILURE", () => {
      const { container } = render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "FAILURE" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if targetEnv is undefined", () => {
      const { container } = render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, targetEnv: undefined }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if sourceEnv is undefined", () => {
      const { container } = render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, sourceEnv: undefined }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if targetEnvId is undefined", () => {
      const { container } = render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, targetEnvId: undefined }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );

      expect(container).toBeEmptyDOMElement();
    });
  });

  describe("handles banner for entity with an open request (type schema)", () => {
    beforeAll(() => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={promotionDetails}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={true}
        />
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `There is an open schema request for ${testTopicName}.`
      );

      expect(information).toBeVisible();
    });

    it("shows a button to open requests", () => {
      const button = screen.getByRole("button", { name: "See the request" });

      expect(button).toBeEnabled();
    });
  });

  describe("handles banner for entity with an open request (type topic)", () => {
    beforeAll(() => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={promotionDetails}
          type={"topic"}
          promoteElement={<></>}
          hasOpenRequest={true}
        />
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `There is an open topic request for ${testTopicName}.`
      );

      expect(information).toBeVisible();
    });

    it("shows a button to open requests", () => {
      const button = screen.getByRole("button", { name: "See the request" });

      expect(button).toBeEnabled();
    });
  });

  describe("handles banner for entity with an open promotion request (type schema)", () => {
    beforeAll(() => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "REQUEST_OPEN" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `There is already an open promotion request for ${testTopicName}.`
      );

      expect(information).toBeVisible();
    });

    it("shows a button to open requests", () => {
      const button = screen.getByRole("button", { name: "See the request" });

      expect(button).toBeEnabled();
    });
  });

  describe("handles banner for entity with an open promotion request (type topic)", () => {
    beforeAll(() => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "REQUEST_OPEN" }}
          type={"topic"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `There is already an open promotion request for ${testTopicName}.`
      );

      expect(information).toBeVisible();
    });

    it("shows a button to open requests", () => {
      const button = screen.getByRole("button", {
        name: "See the request",
      });

      expect(button).toBeVisible();
    });
  });

  describe("handles banner for entity that can be promoted (type schema)", () => {
    const promoteElement = <div data-testid={"another-test-promote-element"} />;
    beforeAll(() => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={promotionDetails}
          type={"schema"}
          promoteElement={promoteElement}
          hasOpenRequest={false}
        />
      );
    });

    afterAll(cleanup);

    it("shows information about possible promotion", () => {
      const information = screen.getByText(
        `This schema has not yet been promoted to the ${promotionDetails.targetEnv} environment.`
      );

      expect(information).toBeVisible();
    });

    it("renders a given component as element handling the promotion", () => {
      const promotionElement = screen.getByTestId(
        "another-test-promote-element"
      );

      expect(promotionElement).toBeVisible();
    });
  });

  describe("handles banner for entity that can be promoted (type topic)", () => {
    const promoteElement = <div data-testid={"test-promote-element"} />;
    beforeAll(() => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={promotionDetails}
          type={"topic"}
          promoteElement={promoteElement}
          hasOpenRequest={false}
        />
      );
    });

    afterAll(cleanup);

    it("shows information about possible promotion", () => {
      const information = screen.getByText(
        `This topic has not yet been promoted to the ${promotionDetails.targetEnv} environment.`
      );

      expect(information).toBeVisible();
    });

    it("renders a given component as element handling the promotion", () => {
      const promotionElement = screen.getByTestId("test-promote-element");

      expect(promotionElement).toBeVisible();
    });
  });

  // @TODO DS external link does not support
  // internal links and we don't have a component
  // from DS that supports that yet. We've to use a
  // button that calls navigate() onClick to support
  // routing in Coral. This should be changed soon
  // and we don't need this tests. So they are separated
  // in a block to make refactoring faster.
  describe("handles navigating", () => {
    afterEach(() => {
      cleanup();
      jest.resetAllMocks();
    });

    it("handles navigating for entity with an open request (type schema)", async () => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={promotionDetails}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={true}
        />
      );
      const button = screen.getByRole("button", { name: "See the request" });

      await user.click(button);

      expect(mockedNavigate).toHaveBeenCalledWith(
        "/requests/schemas?search=my-test-topic&status=CREATED&page=1"
      );
    });

    it("handles navigating for entity with an open request (type topic)", async () => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={promotionDetails}
          type={"topic"}
          promoteElement={<></>}
          hasOpenRequest={true}
        />
      );

      const button = screen.getByRole("button", { name: "See the request" });

      await user.click(button);

      expect(mockedNavigate).toHaveBeenCalledWith(
        "/requests/topics?search=my-test-topic&status=CREATED&page=1"
      );
    });

    it("handles navigating for entity with an open promotion request (type schema)", async () => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "REQUEST_OPEN" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );

      const button = screen.getByRole("button", { name: "See the request" });

      await user.click(button);

      expect(mockedNavigate).toHaveBeenCalledWith(
        "/requests/schemas?search=my-test-topic&requestType=PROMOTE&status=CREATED&page=1"
      );
    });

    it("handles navigating for entity with an open promotion request (type topic)", async () => {
      render(
        <PromotionBanner
          topicName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "REQUEST_OPEN" }}
          type={"topic"}
          promoteElement={<></>}
          hasOpenRequest={false}
        />
      );

      const button = screen.getByRole("button", { name: "See the request" });

      await user.click(button);

      expect(mockedNavigate).toHaveBeenCalledWith(
        "/requests/topics?search=my-test-topic&requestType=PROMOTE&status=CREATED&page=1"
      );
    });
  });
});
