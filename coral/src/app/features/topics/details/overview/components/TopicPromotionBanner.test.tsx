import { screen } from "@testing-library/react";
import { TopicPromotionBanner } from "src/app/features/topics/details/overview/components/TopicPromotionBanner";
import { AuthUser } from "src/domain/auth-user";
import { TopicRequest } from "src/domain/topic";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const authUser: AuthUser = {
  canSwitchTeams: "false",
  teamId: "2",
  teamname: "DS9",
  username: "odo",
};

jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => {
    return authUser;
  },
}));

const promoteProps = {
  isTopicOwner: true,
  topicPromotionDetails: {
    status: "success",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
    topicName: "topic-hello",
  },
  existingPromotionRequest: undefined,
};

const approveProps = {
  topicPromotionDetails: {
    status: "success",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
    topicName: "topic-hello",
  },
  existingPromotionRequest: {
    requestor: "bsisko",
    teamName: "DS9",
    status: "CREATED" as TopicRequest["requestStatus"],
  },
};

const seeProps = {
  topicPromotionDetails: {
    status: "success",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
    topicName: "topic-hello",
  },
  existingPromotionRequest: {
    requestor: "odo",
    teamName: "DS9",
    status: "CREATED" as TopicRequest["requestStatus"],
  },
};

const nullProps = {
  topicPromotionDetails: {
    status: "NO_PROMOTION",
    topicName: "SchemaTest",
  },
};

describe("TopicPromotionBanner (with promotion banner)", () => {
  it("renders correct banner (promote topic)", () => {
    customRender(<TopicPromotionBanner {...promoteProps} />, {
      memoryRouter: true,
      queryClient: true,
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

  it("renders correct banner (approve promotion request)", () => {
    customRender(<TopicPromotionBanner {...approveProps} />, {
      memoryRouter: true,
      queryClient: true,
    });

    const button = screen.getByRole("button", { name: "Approve the request" });
    const link = screen.getByRole("link", {
      name: "Approve the request",
    });

    expect(button).toBeVisible();
    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/approvals/topics?search=${approveProps.topicPromotionDetails.topicName}&page=1&requestType=PROMOTE`
    );
  });

  it("renders correct banner (see promotion request)", () => {
    customRender(<TopicPromotionBanner {...seeProps} />, {
      memoryRouter: true,
      queryClient: true,
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
      `/requests/topics?search=${approveProps.topicPromotionDetails.topicName}&page=1&requestType=PROMOTE`
    );
  });

  it("renders nothing (status === 'NO_PROMOTION')", () => {
    const { container } = customRender(
      <TopicPromotionBanner {...nullProps} />,
      {
        memoryRouter: true,
        queryClient: true,
      }
    );

    expect(container).toBeEmptyDOMElement();
  });
});
