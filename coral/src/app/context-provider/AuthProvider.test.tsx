import { cleanup, screen, within } from "@testing-library/react";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import {
  AuthProvider,
  useAuthContext,
} from "src/app/context-provider/AuthProvider";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { AuthUser } from "src/domain/auth-user";

const getAuthMock = jest.fn();
jest.mock("src/domain/auth-user", () => ({
  ...jest.requireActual("src/domain/auth-user"),
  getAuth: () => getAuthMock(),
}));

const testAuthUser: AuthUser = {
  canSwitchTeams: "",
  teamId: "",
  teamname: "",
  username: "Jon Snow",
};

const ChildComponent = () => {
  const authUser = useAuthContext();

  return <div data-testid={"auth-provider-child"}>{authUser?.username}</div>;
};

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
      getAuthMock.mockReturnValue(testAuthUser);
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
});
