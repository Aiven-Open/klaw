import { cleanup, screen, within } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  AuthProvider,
  useAuthContext,
} from "src/app/context-provider/AuthProvider";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";
import { setupFeatureFlagMock } from "src/services/feature-flags/test-helper";
import { FeatureFlag } from "src/services/feature-flags/types";

const getAuthMock = jest.fn();
jest.mock("src/domain/auth-user", () => ({
  ...jest.requireActual("src/domain/auth-user"),
  getAuth: () => getAuthMock(),
}));

const ChildComponent = () => {
  const authUser = useAuthContext();

  return <div data-testid={"auth-provider-child"}>{authUser?.username}</div>;
};

const mockAuthUser = { ...testAuthUser, username: "Jon Snow" };
describe("AuthProvider.tsx", () => {
  describe("gets the auth user", () => {
    beforeEach(() => {
      getAuthMock.mockReturnValue({});
      customRender(
        <AuthProvider>
          <ChildComponent />
        </AuthProvider>,
        {
          queryClient: true,
        }
      );
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("shows skeleton page with loading information while auth user is being fetched", () => {
      const loadingInfo = screen.getByText("Loading Klaw");
      expect(loadingInfo).toBeVisible();
    });

    it("calls the auth api", () => {
      expect(getAuthMock).toHaveBeenCalledTimes(1);
    });
  });

  describe("renders an auth provider with given children when auth user is available", () => {
    beforeEach(async () => {
      getAuthMock.mockReturnValue(mockAuthUser);
      customRender(
        <AuthProvider>
          <ChildComponent />
        </AuthProvider>,
        {
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByText("Loading Klaw"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("returns an context provider with given children when user", () => {
      const childElement = screen.getByTestId("auth-provider-child");
      expect(childElement).toBeVisible();
    });

    it("makes context available in child element", () => {
      const childElement = screen.getByTestId("auth-provider-child");
      const authUserFromContext = within(childElement).getByText("Jon Snow");

      expect(authUserFromContext).toBeVisible();
    });
  });

  describe("does not render the auth provider with given children when auth user is SUPERADMIN", () => {
    beforeEach(async () => {
      getAuthMock.mockReturnValue({ mockAuthUser, userrole: "SUPERADMIN" });
      customRender(
        <AuthProvider>
          <ChildComponent />
        </AuthProvider>,
        {
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByText("Loading Klaw"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("does not returns context provider with given children", () => {
      const childElement = screen.queryByTestId("auth-provider-child");
      expect(childElement).not.toBeInTheDocument();
    });

    it("shows a dialog informing superadmin they can not access coral", async () => {
      const dialog = await screen.findByRole("dialog", {
        name: "You're currently logged in as superadmin.",
      });

      expect(dialog).toBeVisible();
    });
  });

  describe("render the auth provider with given children when auth user is SUPERADMIN and feature flag is enabled", () => {
    beforeEach(async () => {
      setupFeatureFlagMock(
        FeatureFlag.FEATURE_FLAG_SUPER_ADMIN_ACCESS_CORAL,
        true
      );
      getAuthMock.mockReturnValue({ mockAuthUser, userrole: "SUPERADMIN" });
      customRender(
        <AuthProvider>
          <ChildComponent />
        </AuthProvider>,
        {
          queryClient: true,
        }
      );

      await waitForElementToBeRemoved(screen.getByText("Loading Klaw"));
    });

    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("returns context provider with given children", () => {
      const childElement = screen.getByTestId("auth-provider-child");
      expect(childElement).toBeVisible();
    });

    it("does not show dialog informing superadmin they can not access coral", () => {
      const dialog = screen.queryByText(
        "You're currently logged in as superadmin."
      );

      expect(dialog).not.toBeInTheDocument();
    });
  });
});
