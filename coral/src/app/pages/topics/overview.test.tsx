import { cleanup, screen, render } from "@testing-library/react";
import { TopicOverviewPage } from "src/app/pages/topics/overview";

const mockedUsedNavigate = vi.fn();
vi.mock("react-router-dom", () => ({
  ...vi.importActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

describe("TopicOverviewPage", () => {
  beforeEach(() => {
    render(<TopicOverviewPage />);
  });

  afterEach(cleanup);

  it("shows the dummy placeholder text", () => {
    const text = screen.getByText("<TopicOverview /> goes here");

    expect(text).toBeVisible();
  });
});
