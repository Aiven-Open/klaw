import { cleanup, screen, render } from "@testing-library/react";
import { TopicOverviewPage } from "src/app/pages/topics/overview";

const mockedUsedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

describe("TopicOverviewPage", () => {
  beforeAll(() => {
    render(<TopicOverviewPage />);
  });

  afterAll(cleanup);

  it("shows the dummy placeholder text", () => {
    const text = screen.getByText("<TopicOverview /> goes here");

    expect(text).toBeVisible();
  });
});
