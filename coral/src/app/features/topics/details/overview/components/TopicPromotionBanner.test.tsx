import { cleanup, render, screen } from "@testing-library/react";
import { TopicPromotionBanner } from "src/app/features/topics/details/overview/components/TopicPromotionBanner";
import { TopicOverview } from "src/domain/topic";
import userEvent from "@testing-library/user-event";

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

const promotionDetailForPromote: TopicOverview["topicPromotionDetails"] = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
};

const promoteProps = {
  topicPromotionDetails: promotionDetailForPromote,
  hasOpenTopicRequest: false,
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
  topicName: "topic-hello",
};

const promotionDetailForNoPromotion: TopicOverview["topicPromotionDetails"] = {
  status: "NO_PROMOTION",
};

const nullProps = {
  topicPromotionDetails: promotionDetailForNoPromotion,
  hasOpenTopicRequest: false,
  topicName: "topic-hello",
};

describe("TopicPromotionBanner", () => {
  const user = userEvent.setup();

  afterEach(() => {
    jest.resetAllMocks();
    cleanup();
  });

  it("renders correct banner (promote topic)", () => {
    render(<TopicPromotionBanner {...promoteProps} />);

    const button = screen.getByRole("button", {
      name: "Promote",
    });

    expect(button).toBeEnabled();
  });

  it("enables navigating correctly (promote topic)", async () => {
    render(<TopicPromotionBanner {...promoteProps} />);
    const button = screen.getByRole("button", {
      name: "Promote",
    });

    await user.click(button);

    expect(mockedNavigate).toHaveBeenCalledWith(
      `/topic/${promoteProps.topicName}/request-promotion?sourceEnv=${promoteProps.topicPromotionDetails.sourceEnv}&targetEnv=${promoteProps.topicPromotionDetails.targetEnvId}`
    );
  });

  it("renders correct banner (see open request)", () => {
    render(<TopicPromotionBanner {...seeOpenRequestProps} />);

    const button = screen.getByRole("button", {
      name: "See the request",
    });

    expect(button).toBeEnabled();
  });

  it("enables navigating correctly (see open request)", async () => {
    render(<TopicPromotionBanner {...seeOpenRequestProps} />);

    const button = screen.getByRole("button", {
      name: "See the request",
    });

    await user.click(button);
    expect(mockedNavigate).toHaveBeenCalledWith(
      `/requests/topics?search=${promoteProps.topicName}&status=CREATED&page=1`
    );
  });

  it("renders correct banner (see open promotion request)", () => {
    render(<TopicPromotionBanner {...seeOpenPromotionRequestProps} />);

    const button = screen.getByRole("button", {
      name: "See the request",
    });

    expect(button).toBeEnabled();
  });

  it("enables navigating correctly (see open promotion request)", async () => {
    render(<TopicPromotionBanner {...seeOpenPromotionRequestProps} />);

    const button = screen.getByRole("button", {
      name: "See the request",
    });
    await user.click(button);

    expect(mockedNavigate).toHaveBeenCalledWith(
      `/requests/topics?search=${promoteProps.topicName}&requestType=PROMOTE&status=CREATED&page=1`
    );
  });

  it("renders nothing if status === 'NO_PROMOTION'", () => {
    render(<TopicPromotionBanner {...nullProps} />);

    const wrapper = screen.getByTestId("topic-promotion-banner");

    expect(wrapper).toBeEmptyDOMElement();
  });
});
