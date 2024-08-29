import {
  cleanup,
  render,
  renderHook,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
import {
  AuthProvider,
  useAuthContext,
} from "src/app/context-provider/AuthProvider";
import { waitForElementToBeRemoved } from "@testing-library/react/pure";
import { testAuthUser } from "src/domain/auth-user/auth-user-test-helper";
import { QueryClientProvider } from "@tanstack/react-query";
import { getQueryClientForTests } from "src/services/test-utils/query-client-tests";

const getAuthMock = jest.fn();
jest.mock("src/domain/auth-user", () => ({
  ...jest.requireActual("src/domain/auth-user"),
  getAuth: () => getAuthMock(),
}));

const AllProviders = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={getQueryClientForTests()}>
    <AuthProvider>{children}</AuthProvider>
  </QueryClientProvider>
);

const ChildComponent = () => {
  const authUser = useAuthContext();

  return <div data-testid={"auth-provider-child"}>{authUser?.username}</div>;
};

const mockAuthUser = { ...testAuthUser, username: "Jon Snow" };
const mockAuthSuperAdminUser = {
  ...testAuthUser,
  username: "Arya Stark",
  userrole: "SUPERADMIN",
};

describe("AuthProvider.tsx", () => {
  describe("gets the auth user", () => {
    beforeEach(() => {
      getAuthMock.mockReturnValue({});
      render(
        <AllProviders>
          <ChildComponent />
        </AllProviders>
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
      render(
        <AllProviders>
          <ChildComponent />
        </AllProviders>
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

  describe("useAuthContext hook", () => {
    afterEach(() => {
      jest.resetAllMocks();
      cleanup();
    });

    it("returns the correct user data and identifies a SUPERADMIN user", async () => {
      getAuthMock.mockResolvedValue(mockAuthSuperAdminUser);

      const { result } = renderHook(() => useAuthContext(), {
        wrapper: AllProviders,
      });

      await waitFor(() => {
        expect(result.current.username).toEqual("Arya Stark");
        expect(result.current.isSuperAdminUser).toBe(true);
      });
    });

    it("returns the correct user data and identifies a non-SUPERADMIN user", async () => {
      getAuthMock.mockResolvedValue(mockAuthUser);
      const { result } = renderHook(() => useAuthContext(), {
        wrapper: AllProviders,
      });

      await waitFor(() => {
        expect(result.current.username).toEqual("Jon Snow");
        expect(result.current.isSuperAdminUser).toBe(false);
      });
    });
  });
});
