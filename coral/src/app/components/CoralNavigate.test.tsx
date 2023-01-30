import { cleanup, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import CoralNavigate from "src/app/components/CoralNavigate";

const NavigateMock = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  Navigate: (args: { to: string }) => {
    NavigateMock(args);
    return (
      <div data-testid="mockedRedirectComponent">
        This dummy component represents &lt;Navigate&gt;
      </div>
    );
  },
}));

describe("CoralNavigate", () => {
  const locationAssignSpy = jest.fn();
  let originalLocation: Location;
  beforeAll(() => {
    originalLocation = window.location;
    Object.defineProperty(global.window, "location", {
      writable: true,
      value: {
        assign: locationAssignSpy,
      },
    });
  });
  afterAll(() => {
    global.window.location = originalLocation;
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe("when using legacy paths", () => {
    beforeAll(() => {
      render(<CoralNavigate to="/this-is-absolute-path" useLegacy={true} />);
    });
    afterAll(() => {
      cleanup();
    });
    it("calls window.location.assign with correct value", () => {
      expect(locationAssignSpy).toHaveBeenCalledTimes(1);
      expect(locationAssignSpy).toHaveBeenCalledWith("/this-is-absolute-path");
    });
    it("does not render React Router <Navigate />", () => {
      expect(screen.queryByTestId("mockedRedirectComponent")).toBeNull();
      expect(locationAssignSpy).not.toHaveBeenCalledWith();
    });
  });

  describe("when using non legacy", () => {
    beforeAll(() => {
      render(
        <MemoryRouter>
          <CoralNavigate to="/this-is-absolute-path" useLegacy={false} />
        </MemoryRouter>
      );
    });
    afterAll(() => {
      cleanup();
    });
    it("does not render React Router <Navigate />", () => {
      screen.getByTestId("mockedRedirectComponent");
      expect(NavigateMock).toHaveBeenCalledTimes(1);
      expect(NavigateMock).toHaveBeenCalledWith({
        to: "/this-is-absolute-path",
      });
    });

    it("does not call window.location.assign", () => {
      expect(locationAssignSpy).not.toHaveBeenCalled();
    });
  });
});
