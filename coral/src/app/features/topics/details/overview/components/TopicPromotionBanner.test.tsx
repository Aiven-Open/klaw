import { cleanup, render, screen } from "@testing-library/react";
import { TopicPromotionBanner } from "src/app/features/topics/details/overview/components/TopicPromotionBanner";
import { TopicOverview } from "src/domain/topic";

const promotionDetailForPromote: TopicOverview["topicPromotionDetails"] = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
  topicName: "topic-hello",
};

const promoteProps = {
  topicPromotionDetails: promotionDetailForPromote,
  hasOpenRequest: false,
};

const promotionDetailForSeeOpenRequest: TopicOverview["topicPromotionDetails"] =
  {
    status: "SUCCESS",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
    topicName: "topic-hello",
  };

const seeOpenRequestProps = {
  topicPromotionDetails: promotionDetailForSeeOpenRequest,
  hasOpenRequest: true,
};

const promotionDetailForSeeOpenPromotionRequest: TopicOverview["topicPromotionDetails"] =
  {
    status: "REQUEST_OPEN",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
    topicName: "topic-hello",
  };

const seeOpenPromotionRequestProps = {
  topicPromotionDetails: promotionDetailForSeeOpenPromotionRequest,
  hasOpenRequest: false,
};

const promotionDetailForNoPromotion: TopicOverview["topicPromotionDetails"] = {
  status: "NO_PROMOTION",
  topicName: "SchemaTest",
};

const nullProps = {
  topicPromotionDetails: promotionDetailForNoPromotion,
  hasOpenRequest: false,
};

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

describe("TopicPromotionBanner", () => {
  afterEach(cleanup);

  it("renders correct banner (promote topic)", () => {
    render(<TopicPromotionBanner {...promoteProps} />);

    const link = screen.getByRole("link", {
      name: "Promote",
    });

    expect(link).toBeEnabled();
    expect(link).toHaveAttribute(
      "href",
      `/topic/${promoteProps.topicPromotionDetails.topicName}/request-promotion?sourceEnv=${promoteProps.topicPromotionDetails.sourceEnv}&targetEnv=${promoteProps.topicPromotionDetails.targetEnvId}`
    );
  });

  it("renders correct banner (see open request)", () => {
    render(<TopicPromotionBanner {...seeOpenRequestProps} />);

    const link = screen.getByRole("link", {
      name: "See the request",
    });

    expect(link).toBeEnabled();
    expect(link).toHaveAttribute(
      "href",
      `/requests/topics?search=${promoteProps.topicPromotionDetails.topicName}&status=CREATED&page=1`
    );
  });

  it("renders correct banner (see open promotion request)", () => {
    render(<TopicPromotionBanner {...seeOpenPromotionRequestProps} />);

    const link = screen.getByRole("link", {
      name: "See the request",
    });

    expect(link).toBeEnabled();
    expect(link).toHaveAttribute(
      "href",
      `/requests/topics?search=${promoteProps.topicPromotionDetails.topicName}&requestType=PROMOTE&status=CREATED&page=1`
    );
  });

  it("renders nothing if status === 'NO_PROMOTION'", () => {
    render(<TopicPromotionBanner {...nullProps} />);

    const wrapper = screen.getByTestId("topic-promotion-banner");

    expect(wrapper).toBeEmptyDOMElement();
  });
});
