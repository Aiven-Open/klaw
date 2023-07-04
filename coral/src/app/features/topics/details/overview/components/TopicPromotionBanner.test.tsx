import { screen } from "@testing-library/react";
import { TopicPromotionBanner } from "src/app/features/topics/details/overview/components/TopicPromotionBanner";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const promoteProps = {
  isTopicOwner: true,
  topicPromotionDetails: {
    status: "success",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
    topicName: "topic-hello",
  },
  hasOpenRequest: false,
};

const seeProps = {
  topicPromotionDetails: {
    status: "success",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
    topicName: "topic-hello",
  },
  hasOpenRequest: true,
};

const nullProps = {
  topicPromotionDetails: {
    status: "NO_PROMOTION",
    topicName: "SchemaTest",
  },
  hasOpenRequest: false,
};

describe("TopicPromotionBanner (with promotion banner)", () => {
  it("renders correct banner (promote topic)", () => {
    customRender(<TopicPromotionBanner {...promoteProps} />, {
      memoryRouter: true,
    });

    const button = screen.getByRole("button", { name: "Promote" });
    const link = screen.getByRole("link", {
      name: "Promote",
    });

    expect(button).toBeVisible();
    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/topic/${promoteProps.topicPromotionDetails.topicName}/request-promotion?sourceEnv=${promoteProps.topicPromotionDetails.sourceEnv}&targetEnv=${promoteProps.topicPromotionDetails.targetEnvId}`
    );
  });

  it("renders correct banner (see promotion request)", () => {
    customRender(<TopicPromotionBanner {...seeProps} />, {
      memoryRouter: true,
    });

    const button = screen.getByRole("button", {
      name: "See the request",
    });
    const link = screen.getByRole("link", {
      name: "See the request",
    });

    expect(button).toBeVisible();
    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
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
