import { cleanup, render, screen } from "@testing-library/react";
import { SchemaPromotionBanner } from "src/app/features/topics/details/schema/components/SchemaPromotionBanner";
import { TopicSchemaOverview } from "src/domain/topic";
import userEvent from "@testing-library/user-event";

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
    render(
      <SchemaPromotionBanner
        schemaPromotionDetails={schemaPromotionDetailsBase}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={false}
      />
    );

    const button = screen.getByRole("button", {
      name: "Promote",
    });

    expect(button).toBeEnabled();
  });

  it("enables user to promote a schema", async () => {
    render(
      <SchemaPromotionBanner
        schemaPromotionDetails={schemaPromotionDetailsBase}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={false}
      />
    );

    const button = screen.getByRole("button", {
      name: "Promote",
    });

    await user.click(button);

    expect(mockSetShowSchemaPromotionModal).toHaveBeenCalled();
  });

  it("renders correct banner (see open request)", () => {
    render(
      <SchemaPromotionBanner
        schemaPromotionDetails={schemaPromotionDetailsBase}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={true}
      />
    );

    const button = screen.queryByRole("button", {
      name: "Promote",
    });
    const link = screen.getByRole("link", {
      name: "See the request",
    });

    expect(link).toBeEnabled();
    expect(link).toHaveAttribute(
      "href",
      `/requests/schemas?search=my-test-topic&status=CREATED&page=1`
    );
    expect(button).not.toBeInTheDocument();
  });

  it("renders correct banner (see open promotion request)", () => {
    render(
      <SchemaPromotionBanner
        schemaPromotionDetails={{
          ...schemaPromotionDetailsBase,
          status: "REQUEST_OPEN",
        }}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={false}
      />
    );

    const button = screen.queryByRole("button", {
      name: "Promote",
    });
    const link = screen.getByRole("link", {
      name: "See the request",
    });

    expect(link).toBeEnabled();
    expect(link).toHaveAttribute(
      "href",
      `/requests/schemas?search=my-test-topic&requestType=PROMOTE&status=CREATED&page=1`
    );
    expect(button).not.toBeInTheDocument();
  });

  it("renders nothing if status === 'NO_PROMOTION'", () => {
    render(
      <SchemaPromotionBanner
        schemaPromotionDetails={{
          ...schemaPromotionDetailsBase,
          status: "NO_PROMOTION",
        }}
        topicName={"my-test-topic"}
        setShowSchemaPromotionModal={mockSetShowSchemaPromotionModal}
        hasOpenSchemaRequest={false}
      />
    );

    const wrapper = screen.getByTestId("schema-promotion-banner");

    expect(wrapper).toBeEmptyDOMElement();
  });
});
