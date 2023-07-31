import { cleanup, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { SchemaPromotionBanner } from "src/app/features/topics/details/schema/components/SchemaPromotionBanner";
import { TopicSchemaOverview } from "src/domain/topic";
import { customRender } from "src/services/test-utils/render-with-wrappers";

const schemaPromotionDetailsBase: TopicSchemaOverview["schemaPromotionDetails"] =
  {
    status: "SUCCESS",
    sourceEnv: "DEV",
    targetEnv: "TST",
    targetEnvId: "2",
  };

describe("SchemaPromotionBanner", () => {
  const user = userEvent.setup();

  const mockSetShowSchemaPromotionModal = jest.fn();
  afterEach(() => {
    jest.resetAllMocks();
    cleanup();
  });

  it("renders correct banner (promote schema)", () => {
    customRender(
      <SchemaPromotionBanner
        schemaPromotionDetails={schemaPromotionDetailsBase}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={false}
      />,
      { browserRouter: true }
    );

    const button = screen.getByRole("button", {
      name: "Promote",
    });

    expect(button).toBeEnabled();
  });

  it("enables user to promote a schema", async () => {
    customRender(
      <SchemaPromotionBanner
        schemaPromotionDetails={schemaPromotionDetailsBase}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={false}
      />,
      { browserRouter: true }
    );

    const button = screen.getByRole("button", {
      name: "Promote",
    });

    await user.click(button);

    expect(mockSetShowSchemaPromotionModal).toHaveBeenCalled();
  });

  it("renders correct banner (see open request)", () => {
    customRender(
      <SchemaPromotionBanner
        schemaPromotionDetails={schemaPromotionDetailsBase}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={true}
      />,
      { browserRouter: true }
    );

    const buttonPromote = screen.queryByRole("button", {
      name: "Promote",
    });
    const linkSeeRequest = screen.getByRole("link", {
      name: "See the request",
    });

    expect(linkSeeRequest).toBeVisible();
    expect(linkSeeRequest).toHaveAttribute(
      "href",
      `/requests/schemas?search=my-test-topic&status=CREATED&page=1`
    );
    expect(buttonPromote).not.toBeInTheDocument();
  });

  it("renders correct banner (see open promotion request)", () => {
    customRender(
      <SchemaPromotionBanner
        schemaPromotionDetails={{
          ...schemaPromotionDetailsBase,
          status: "REQUEST_OPEN",
        }}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={false}
      />,
      { browserRouter: true }
    );

    const buttonPromote = screen.queryByRole("button", {
      name: "Promote",
    });
    const linkSeeRequest = screen.getByRole("link", {
      name: "See the request",
    });

    expect(linkSeeRequest).toBeVisible();
    expect(linkSeeRequest).toHaveAttribute(
      "href",
      "/requests/schemas?search=my-test-topic&requestType=CREATE&status=CREATED&page=1"
    );
    expect(buttonPromote).not.toBeInTheDocument();
  });

  it("renders nothing if status === 'NO_PROMOTION'", () => {
    customRender(
      <SchemaPromotionBanner
        schemaPromotionDetails={{
          ...schemaPromotionDetailsBase,
          status: "NO_PROMOTION",
        }}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={false}
      />,
      { browserRouter: true }
    );

    const wrapper = screen.getByTestId("schema-promotion-banner");

    expect(wrapper).toBeEmptyDOMElement();
  });
});
