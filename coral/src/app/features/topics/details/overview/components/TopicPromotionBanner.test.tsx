import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TopicPromotionBanner } from "src/app/features/topics/details/overview/components/TopicPromotionBanner";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { TopicOverview } from "src/domain/topic";

const promotionDetailForPromote: TopicOverview["topicPromotionDetails"] = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
  topicName: "topic-hello",
};

const promoteProps = {
  isTopicOwner: true,
  topicPromotionDetails: promotionDetailForPromote,
  hasOpenRequest: false,
};

const promotionDetailForSee: TopicOverview["topicPromotionDetails"] = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
  topicName: "topic-hello",
};
const seeProps = {
  isTopicOwner: false,
  topicPromotionDetails: promotionDetailForSee,
  hasOpenRequest: true,
};

const promotionDetailForNoPromotion: TopicOverview["topicPromotionDetails"] = {
  status: "NO_PROMOTION",
  topicName: "SchemaTest",
};

const nullProps = {
  isTopicOwner: false,
  topicPromotionDetails: promotionDetailForNoPromotion,
  hasOpenRequest: false,
};

const mockedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
}));

describe("TopicPromotionBanner (with promotion banner)", () => {
  it("renders correct banner (promote topic)", async () => {
    customRender(<TopicPromotionBanner {...promoteProps} />, {
      memoryRouter: true,
    });

    const button = screen.getByRole("button", {
      name: "Promote",
    });

    expect(button).toBeEnabled();

    await userEvent.click(button);
    expect(mockedNavigate).toHaveBeenCalledWith(
      `/topic/${promoteProps.topicPromotionDetails.topicName}/request-promotion?sourceEnv=${promoteProps.topicPromotionDetails.sourceEnv}&targetEnv=${promoteProps.topicPromotionDetails.targetEnvId}`
    );
  });

  it("renders correct banner (see promotion request)", async () => {
    customRender(<TopicPromotionBanner {...seeProps} />, {
      memoryRouter: true,
    });

    const button = screen.getByRole("button", {
      name: "See the request",
    });

    expect(button).toBeEnabled();

    await userEvent.click(button);

    expect(mockedNavigate).toHaveBeenCalledWith(
      `/requests/topics?search=${promoteProps.topicPromotionDetails.topicName}&status=CREATED&page=1`
    );
  });

  it("renders nothing (status === 'NO_PROMOTION')", () => {
    const { container } = customRender(
      <TopicPromotionBanner {...nullProps} />,
      {
        memoryRouter: true,
      }
    );

    expect(container).toBeEmptyDOMElement();
  });
});
