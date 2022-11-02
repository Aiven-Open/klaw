import HomePage from "src/app/pages";
import { cleanup, screen } from "@testing-library/react";
import { renderWithQueryClient } from "src/services/test-utils";

const loadingText = "data is loading";
const textFromApi = "hello";

describe("HomePage", () => {
  beforeEach(() => {
    renderWithQueryClient(<HomePage />);
  });

  afterEach(() => {
    cleanup();
  });

  it("renders dummy content", () => {
    const heading = screen.getByRole("heading", { name: "Index" });

    expect(heading).toBeVisible();
  });

  it("shows a loading information as long as data is not returned from 'API'", () => {
    const loadingInfo = screen.getByText(loadingText);

    expect(loadingInfo).toBeVisible();
  });

  it("shows the data returned from the 'API'", async () => {
    const dataFromApi = await screen.findByText(textFromApi);

    expect(dataFromApi).toBeVisible();
  });

  it("removes loading information when data is returned from the 'API'", async () => {
    const loadingInfo = screen.queryByText(loadingText);
    expect(loadingInfo).toBeVisible();

    await screen.findByText(textFromApi);

    expect(loadingInfo).not.toBeInTheDocument();
  });
});
