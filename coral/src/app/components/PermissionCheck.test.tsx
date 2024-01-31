// FILEPATH: /Users/mathieu.anderson/Documents/klaw/coral/src/app/components/PermissionsCheck.test.tsx
import { cleanup, render, screen } from "@testing-library/react";
import PermissionCheck from "src/app/components/PermissionCheck";

const mockAuthUser = jest.fn();
jest.mock("src/app/context-provider/AuthProvider", () => ({
  useAuthContext: () => mockAuthUser(),
}));

describe("PermissionCheck", () => {
  afterEach(() => {
    jest.resetAllMocks();
    cleanup();
  });

  it("renders children when permission is true", () => {
    mockAuthUser.mockReturnValue({
      permissions: { approveDeclineTopics: true },
    });

    render(
      <PermissionCheck permission="approveDeclineTopics">
        <div data-testid="child-component">Child Component</div>
      </PermissionCheck>
    );

    expect(screen.getByTestId("child-component")).toBeInTheDocument();
  });

  it("does not render children when permission is false", () => {
    mockAuthUser.mockReturnValue({
      permissions: { approveDeclineTopics: false },
    });

    render(
      <PermissionCheck permission="approveDeclineTopics">
        <div data-testid="child-component">Child Component</div>
      </PermissionCheck>
    );

    expect(screen.queryByTestId("child-component")).not.toBeInTheDocument();
  });

  it("renders placeholder when permission is false and placeholder is provided", () => {
    mockAuthUser.mockReturnValue({
      permissions: { approveDeclineTopics: false },
    });

    render(
      <PermissionCheck
        permission="approveDeclineTopics"
        placeholder={<div data-testid="placeholder">Placeholder</div>}
      >
        <div data-testid="child-component">Child Component</div>
      </PermissionCheck>
    );

    expect(screen.queryByTestId("child-component")).not.toBeInTheDocument();
    expect(screen.getByTestId("placeholder")).toBeInTheDocument();
  });
});
