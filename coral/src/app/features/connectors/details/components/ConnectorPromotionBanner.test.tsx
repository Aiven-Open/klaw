import { cleanup, screen } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  ConnectorPromotionBanner,
  TemporaryConnectorPromotionDetails,
} from "src/app/features/connectors/details/components/ConnectorPromotionBanner";

const promotionDetailForPromote: TemporaryConnectorPromotionDetails = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
};

const promoteProps = {
  connectorPromotionDetails: promotionDetailForPromote,
  hasOpenConnectorRequest: false,
  hasOpenClaimRequest: false,
  connectorName: "my-nice-connector-name",
};

const promotionDetailForSeeOpenRequest: TemporaryConnectorPromotionDetails = {
  status: "SUCCESS",
  targetEnv: "TST",
  sourceEnv: "DEV",
  targetEnvId: "2",
};

const seeOpenRequestProps = {
  connectorPromotionDetails: promotionDetailForSeeOpenRequest,
  hasOpenConnectorRequest: true,
  hasOpenClaimRequest: false,
  connectorName: "my-nice-connector-name",
};

const seeOpenClaimRequestProps = {
  connectorPromotionDetails: promotionDetailForSeeOpenRequest,
  hasOpenConnectorRequest: false,
  hasOpenClaimRequest: true,
  connectorName: "my-nice-connector-name",
};

const promotionDetailForSeeOpenPromotionRequest: TemporaryConnectorPromotionDetails =
  {
    status: "REQUEST_OPEN",
    targetEnv: "TST",
    sourceEnv: "DEV",
    targetEnvId: "2",
  };

const seeOpenPromotionRequestProps = {
  connectorPromotionDetails: promotionDetailForSeeOpenPromotionRequest,
  hasOpenConnectorRequest: false,
  hasOpenClaimRequest: false,
  connectorName: "my-nice-connector-name",
};

const promotionDetailForNoPromotion: TemporaryConnectorPromotionDetails = {
  status: "NO_PROMOTION",
};

const nullProps = {
  connectorPromotionDetails: promotionDetailForNoPromotion,
  hasOpenConnectorRequest: false,
  hasOpenClaimRequest: false,
  connectorName: "my-nice-connector-name",
};

describe("ConnectorPromotionBanner", () => {
  afterEach(() => {
    jest.resetAllMocks();
    cleanup();
  });

  it("renders correct banner (promote connector)", () => {
    customRender(<ConnectorPromotionBanner {...promoteProps} />, {
      browserRouter: true,
    });

    const link = screen.getByRole("link", {
      name: "Promote",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/connector/${promoteProps.connectorName}/request-promotion?sourceEnv=${promoteProps.connectorPromotionDetails.sourceEnv}&targetEnv=${promoteProps.connectorPromotionDetails.targetEnvId}`
    );
  });

  it("renders correct banner (see open request)", () => {
    customRender(<ConnectorPromotionBanner {...seeOpenRequestProps} />, {
      browserRouter: true,
    });

    const link = screen.getByRole("link", {
      name: "View request",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/requests/connectors?search=${promoteProps.connectorName}&requestType=ALL&status=CREATED&page=1`
    );
  });

  it("renders correct banner (see open claim request)", () => {
    customRender(<ConnectorPromotionBanner {...seeOpenClaimRequestProps} />, {
      browserRouter: true,
    });

    const link = screen.getByRole("link", {
      name: "View request",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/approvals/connectors?search=${promoteProps.connectorName}&requestType=CLAIM&status=CREATED&page=1`
    );
  });

  it("renders correct banner (see open promotion request)", () => {
    customRender(
      <ConnectorPromotionBanner {...seeOpenPromotionRequestProps} />,
      {
        browserRouter: true,
      }
    );

    const link = screen.getByRole("link", {
      name: "View request",
    });

    expect(link).toBeVisible();
    expect(link).toHaveAttribute(
      "href",
      `/requests/connectors?search=${promoteProps.connectorName}&requestType=PROMOTE&status=CREATED&page=1`
    );
  });

  it("renders nothing if status === 'NO_PROMOTION'", () => {
    customRender(<ConnectorPromotionBanner {...nullProps} />, {
      browserRouter: true,
    });

    const wrapper = screen.getByTestId("connector-promotion-banner");

    expect(wrapper).toBeEmptyDOMElement();
  });
});
