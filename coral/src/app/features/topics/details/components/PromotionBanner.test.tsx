import { cleanup, screen } from "@testing-library/react";
import { PromotionBanner } from "src/app/features/topics/details/components/PromotionBanner";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { KlawApiModel } from "types/utils";

const promotionDetails: KlawApiModel<"PromotionStatus"> = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
};
const testTopicName = "my-test-topic";

describe("PromotionBanner", () => {
  describe("does not show a promotion banner at all dependent on promotion details", () => {
    afterEach(cleanup);

    it("returns null if status is NOT_AUTHORIZED", () => {
      const { container } = customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "NOT_AUTHORIZED" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if status is NO_PROMOTION", () => {
      const { container } = customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "NO_PROMOTION" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if status is FAILURE", () => {
      const { container } = customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "FAILURE" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if targetEnv is undefined", () => {
      const { container } = customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={{ ...promotionDetails, targetEnv: undefined }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if sourceEnv is undefined", () => {
      const { container } = customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={{ ...promotionDetails, sourceEnv: undefined }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );

      expect(container).toBeEmptyDOMElement();
    });

    it("returns null if targetEnvId is undefined", () => {
      const { container } = customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={{ ...promotionDetails, targetEnvId: undefined }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );

      expect(container).toBeEmptyDOMElement();
    });
  });

  describe("handles banner for entity with an open request (type schema)", () => {
    beforeAll(() => {
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={promotionDetails}
          type={"schema"}
          promoteElement={<></>}
          hasOpenClaimRequest={false}
          hasOpenRequest={true}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `You cannot promote the schema at this time. ${testTopicName} has a pending request.`
      );

      expect(information).toBeVisible();
    });

    it("shows a link to open requests", () => {
      const link = screen.getByRole("link", { name: "View request" });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        "/requests/schemas?search=my-test-topic&requestType=ALL&status=CREATED&page=1"
      );
    });
  });

  describe("handles banner for entity with an open request (type topic)", () => {
    beforeAll(() => {
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={promotionDetails}
          type={"topic"}
          promoteElement={<></>}
          hasOpenClaimRequest={false}
          hasOpenRequest={true}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `You cannot promote the topic at this time. ${testTopicName} has a pending request.`
      );

      expect(information).toBeVisible();
    });

    it("shows a link to open requests", () => {
      const link = screen.getByRole("link", { name: "View request" });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        "/requests/topics?search=my-test-topic&requestType=ALL&status=CREATED&page=1"
      );
    });
  });

  describe("handles banner for entity with an open claim request (type schema)", () => {
    beforeAll(() => {
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={promotionDetails}
          type={"schema"}
          promoteElement={<></>}
          hasOpenClaimRequest={true}
          hasOpenRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `You cannot promote the schema at this time. A claim request for ${testTopicName} is in progress.`
      );

      expect(information).toBeVisible();
    });

    it("shows a link to open requests", () => {
      const link = screen.getByRole("link", { name: "View request" });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        "/approvals/schemas?search=my-test-topic&requestType=CLAIM&status=CREATED&page=1"
      );
    });
  });

  describe("handles banner for entity with an open claim request (type topic)", () => {
    beforeAll(() => {
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={promotionDetails}
          type={"topic"}
          promoteElement={<></>}
          hasOpenClaimRequest={true}
          hasOpenRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `You cannot promote the topic at this time. A claim request for ${testTopicName} is in progress.`
      );

      expect(information).toBeVisible();
    });

    it("shows a link to open requests", () => {
      const link = screen.getByRole("link", { name: "View request" });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        "/approvals/topics?search=my-test-topic&requestType=CLAIM&status=CREATED&page=1"
      );
    });
  });

  describe("handles banner for entity with an open promotion request (type schema)", () => {
    beforeAll(() => {
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "REQUEST_OPEN" }}
          type={"schema"}
          promoteElement={<></>}
          hasOpenClaimRequest={false}
          hasOpenRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `You cannot promote the schema at this time. An promotion request for ${testTopicName} is already in progress.`
      );

      expect(information).toBeVisible();
    });

    it("shows a button to open requests", () => {
      const link = screen.getByRole("link", { name: "View request" });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        "/requests/schemas?search=my-test-topic&requestType=PROMOTE&status=CREATED&page=1"
      );
    });
  });

  describe("handles banner for entity with an open promotion request (type topic)", () => {
    beforeAll(() => {
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={{ ...promotionDetails, status: "REQUEST_OPEN" }}
          type={"topic"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
      );
    });

    afterAll(cleanup);

    it("shows information about the open request", () => {
      const information = screen.getByText(
        `You cannot promote the topic at this time. An promotion request for ${testTopicName} is already in progress.`
      );

      expect(information).toBeVisible();
    });

    it("shows a link to open requests", () => {
      const link = screen.getByRole("link", {
        name: "View request",
      });

      expect(link).toBeVisible();
      expect(link).toHaveAttribute(
        "href",
        "/requests/topics?search=my-test-topic&requestType=PROMOTE&status=CREATED&page=1"
      );
    });
  });

  describe("handles banner for entity that can be promoted (type schema)", () => {
    const promoteElement = <div data-testid={"another-test-promote-element"} />;
    beforeAll(() => {
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={promotionDetails}
          type={"schema"}
          promoteElement={promoteElement}
          hasOpenClaimRequest={false}
          hasOpenRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
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
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={promotionDetails}
          type={"topic"}
          promoteElement={promoteElement}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={false}
          errorMessage={""}
        />,
        { browserRouter: true }
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

  describe("handles error for promotion (type schema and topic)", () => {
    const originalConsoleError = console.error;

    beforeEach(() => {
      console.error = jest.fn();
    });

    afterEach(() => {
      console.error = originalConsoleError;
      jest.resetAllMocks();
      cleanup();
    });

    it("shows an alert with a given message", () => {
      const testErrorMessage = "This is an error";
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={promotionDetails}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={true}
          errorMessage={testErrorMessage}
        />,
        { browserRouter: true }
      );

      const alert = screen.getByRole("alert");

      expect(alert).toBeVisible();
      expect(alert).toHaveTextContent(testErrorMessage);
      expect(console.error).not.toHaveBeenCalled();
    });

    it("logs error for developers and shows generic message if errorMessage is empty string", () => {
      customRender(
        <PromotionBanner
          entityName={testTopicName}
          promotionDetails={promotionDetails}
          type={"schema"}
          promoteElement={<></>}
          hasOpenRequest={false}
          hasOpenClaimRequest={false}
          hasError={true}
          errorMessage={""}
        />,
        { browserRouter: true }
      );
      const alert = screen.getByRole("alert");

      expect(alert).toHaveTextContent("Unexpected error.");
      expect(console.error).toHaveBeenCalledWith(
        "Please pass a useful errorMessage for the user!"
      );
    });
  });
});
