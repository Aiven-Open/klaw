import { cleanup, screen } from "@testing-library/react";
import { TopicPromotionBanner } from "src/app/features/topics/details/overview/components/TopicPromotionBanner";
import { TopicOverview } from "src/domain/topic";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const promotionDetailForPromote: TopicOverview["topicPromotionDetails"] = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
};

const promoteProps = {
  topicPromotionDetails: promotionDetailForPromote,
  hasOpenTopicRequest: false,
  hasOpenClaimRequest: false,
  topicName: "topic-hello",
};

const promotionDetailForSeeOpenRequest: TopicOverview["topicPromotionDetails"] =
  {
    status: "SUCCESS",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
  };

const seeOpenRequestProps = {
  topicPromotionDetails: promotionDetailForSeeOpenRequest,
  hasOpenTopicRequest: true,
  hasOpenClaimRequest: false,
  topicName: "topic-hello",
};

const seeOpenClaimRequestProps = {
  topicPromotionDetails: promotionDetailForSeeOpenRequest,
  hasOpenTopicRequest: false,
  hasOpenClaimRequest: true,
  topicName: "topic-hello",
};

const promotionDetailForSeeOpenPromotionRequest: TopicOverview["topicPromotionDetails"] =
  {
    status: "REQUEST_OPEN",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
  };

const seeOpenPromotionRequestProps = {
  topicPromotionDetails: promotionDetailForSeeOpenPromotionRequest,
  hasOpenTopicRequest: false,
  hasOpenClaimRequest: false,
  topicName: "topic-hello",
};

const promotionDetailForNoPromotion: TopicOverview["topicPromotionDetails"] = {
  status: "NO_PROMOTION",
};

const nullProps = {
  topicPromotionDetails: promotionDetailForNoPromotion,
  hasOpenTopicRequest: false,
  hasOpenClaimRequest: false,
  topicName: "topic-hello",
};

describe("TopicPromotionBanner", () => {
  afterEach(() => {
    jest.resetAllMocks();
    cleanup();
  });

  it("renders correct banner (promote topic)", () => {
    customRender(<TopicPromotionBanner {...promoteProps} />, {
      browserRouter: true,
    });

    const link = screen.getByRole("link", {
      name: "Promote",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/topic/${promoteProps.topicName}/request-promotion?sourceEnv=${promoteProps.topicPromotionDetails.sourceEnv}&targetEnv=${promoteProps.topicPromotionDetails.targetEnvId}`
    );
  });

  it("renders correct banner (see open request)", () => {
    customRender(<TopicPromotionBanner {...seeOpenRequestProps} />, {
      browserRouter: true,
    });

    const link = screen.getByRole("link", {
      name: "View request",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/requests/topics?search=${promoteProps.topicName}&requestType=ALL&status=CREATED&page=1`
    );
  });

  it("renders correct banner (see open claim request)", () => {
    customRender(<TopicPromotionBanner {...seeOpenClaimRequestProps} />, {
      browserRouter: true,
    });

    const link = screen.getByRole("link", {
      name: "View request",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/approvals/topics?search=${promoteProps.topicName}&requestType=CLAIM&status=CREATED&page=1`
    );
  });

  it("renders correct banner (see open promotion request)", () => {
    customRender(<TopicPromotionBanner {...seeOpenPromotionRequestProps} />, {
      browserRouter: true,
    });

    const link = screen.getByRole("link", {
      name: "View request",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/requests/topics?search=${promoteProps.topicName}&requestType=PROMOTE&status=CREATED&page=1`
    );
  });

  it("renders nothing if status === 'NO_PROMOTION'", () => {
    customRender(<TopicPromotionBanner {...nullProps} />, {
      browserRouter: true,
    });

    const wrapper = screen.getByTestId("topic-promotion-banner");

    expect(wrapper).toBeEmptyDOMElement();
  });
});
