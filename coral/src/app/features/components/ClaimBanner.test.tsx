import { cleanup, render, screen } from "@testing-library/react";

import { ClaimBanner } from "src/app/features/components/ClaimBanner";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import userEvent from "@testing-library/user-event";

const mockClaimEntity = jest.fn();

const testProps = {
  entityType: "topic",
  entityName: "this-is-a-name",
  claimEntity: mockClaimEntity,
  isError: false,
  hasOpenClaimRequest: false,
  hasOpenRequestOnAnyEnv: false,
  entityOwner: "teamname",
};

describe("ClaimBanner", () => {
  const user = userEvent.setup();

  describe("renders all necessary elements", () => {
    describe("renders banner for entity that can be claimed (type topic)", () => {
      beforeAll(() => {
        render(<ClaimBanner {...testProps} entityType={"topic"} />);
      });

      afterAll(cleanup);

      it("shows information that topic can be claimed", () => {
        const description = screen.getByText(
          `This topic is currently owned by ${testProps.entityOwner}. Select "Claim topic" to request ownership.`
        );

        expect(description).toBeVisible();
      });

      it("shows a button to claim the topic", () => {
        const button = screen.getByRole("button", { name: "Claim topic" });

        expect(button).toBeEnabled();
      });
    });

    describe("renders banner for entity that can be claimed (type connector)", () => {
      beforeAll(() => {
        render(<ClaimBanner {...testProps} entityType={"connector"} />);
      });

      afterAll(cleanup);

      it("shows information that connector can be claimed", () => {
        const description = screen.getByText(
          `This connector is currently owned by ${testProps.entityOwner}. Select "Claim connector" to request ownership.`
        );

        expect(description).toBeVisible();
      });

      it("shows a button to claim the connector", () => {
        const button = screen.getByRole("button", { name: "Claim connector" });

        expect(button).toBeEnabled();
      });
    });

    describe("renders banner for entity with an open request (type topic)", () => {
      beforeAll(() => {
        render(
          <ClaimBanner
            {...testProps}
            entityType={"topic"}
            hasOpenRequestOnAnyEnv={true}
          />
        );
      });

      afterAll(cleanup);

      it("shows information that the topic has a pending request", () => {
        const description = screen.getByText(
          `Your team cannot claim ownership at this time. ${testProps.entityName} has pending requests.`
        );

        expect(description).toBeVisible();
      });
    });

    describe("renders banner for entity with an open request (type connector)", () => {
      beforeAll(() => {
        render(
          <ClaimBanner
            {...testProps}
            entityType={"connector"}
            hasOpenRequestOnAnyEnv={true}
          />
        );
      });

      afterAll(cleanup);

      it("shows information that the connector has a pending request", () => {
        const description = screen.getByText(
          `Your team cannot claim ownership at this time. ${testProps.entityName} has pending requests.`
        );

        expect(description).toBeVisible();
      });
    });

    describe("renders banner for entity with an open claim request (type topic)", () => {
      beforeAll(() => {
        customRender(
          <ClaimBanner
            {...testProps}
            entityType={"topic"}
            hasOpenClaimRequest={true}
            hasOpenRequestOnAnyEnv={true}
          />,
          {
            memoryRouter: true,
          }
        );
      });

      afterAll(cleanup);

      it("shows information that the topic has an open claim request", () => {
        const description = screen.getByText(
          `Your team cannot claim ownership at this time. A claim request for ${testProps.entityName} is already in progress.`
        );

        expect(description).toBeVisible();
      });

      it("shows a link to the claim request", () => {
        const link = screen.getByRole("link", { name: "View request" });

        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          `/requests/topics?search=${testProps.entityName}&requestType=CLAIM&status=CREATED&page=1`
        );
      });
    });

    describe("renders banner for entity with an open claim request (type connector)", () => {
      beforeAll(() => {
        customRender(
          <ClaimBanner
            {...testProps}
            entityType={"connector"}
            hasOpenClaimRequest={true}
            hasOpenRequestOnAnyEnv={true}
          />,
          {
            memoryRouter: true,
          }
        );
      });
      afterAll(cleanup);

      it("shows information that the connector has an open claim request", () => {
        const description = screen.getByText(
          `Your team cannot claim ownership at this time. A claim request for ${testProps.entityName} is already in progress.`
        );

        expect(description).toBeVisible();
      });

      it("shows a link to the claim request", () => {
        const link = screen.getByRole("link", { name: "View request" });

        expect(link).toBeVisible();
        expect(link).toHaveAttribute(
          "href",
          `/requests/connectors?search=${testProps.entityName}&requestType=CLAIM&status=CREATED&page=1`
        );
      });
    });
  });

  describe("renders error state for failed claim process", () => {
    const testErrorMessage = "this is an error";

    afterEach(cleanup);

    it("does not show an error message as long as isError is not true", () => {
      render(
        <ClaimBanner
          {...testProps}
          entityType={"topic"}
          errorMessage={testErrorMessage}
          isError={false}
        />
      );

      const error = screen.queryByText(testErrorMessage);

      expect(error).not.toBeInTheDocument();
    });

    it("shows an error message if isError is true", () => {
      render(
        <ClaimBanner
          {...testProps}
          entityType={"topic"}
          errorMessage={testErrorMessage}
          isError={true}
        />
      );

      const error = screen.getByRole("alert");

      expect(error).toBeVisible();
      expect(error).toHaveTextContent(testErrorMessage);
    });
  });

  describe("enables user to claim a entity", () => {
    beforeEach(() => {
      render(<ClaimBanner {...testProps} entityType={"topic"} />);
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("calls the given function when user clicks button", async () => {
      const button = screen.getByRole("button", { name: "Claim topic" });
      await user.click(button);

      expect(mockClaimEntity).toHaveBeenCalled();
    });
  });
});
