import HomePage from "src/app/pages";
import { screen } from "@testing-library/react";
import { renderWithQueryClient } from "src/services/test-utils";
import { mockUserAuthRequest } from "src/domain/auth-user/auth-user-api.msw";
import { server } from "src/services/api-mocks/server";

const loadingText = "data is loading";
const userName = "Klaw user";

describe("HomePage", () => {
  beforeAll(() => {
    server.listen();
  });

  beforeEach(() => {
    mockUserAuthRequest({
      mswInstance: server,
      userObject: { name: userName },
    });
    renderWithQueryClient(<HomePage />);
  });

  afterEach(() => {
    server.resetHandlers();
  });

  afterAll(() => {
    server.close();
  });

  it("renders dummy content", () => {
    const heading = screen.getByRole("heading", { name: "Index" });

    expect(heading).toBeVisible();
  });

  it("shows a loading information as long as data is not returned from 'API'", () => {
    const loadingInfo = screen.getByText(loadingText);

    expect(loadingInfo).toBeVisible();
  });

  it("shows the username when loaded", async () => {
    const dataFromApi = await screen.findByText(userName);

    expect(dataFromApi).toBeVisible();
  });

  it("removes loading information when data is returned from the 'API'", async () => {
    const loadingInfo = screen.queryByText(loadingText);
    expect(loadingInfo).toBeVisible();

    await screen.findByText(userName);

    expect(loadingInfo).not.toBeInTheDocument();
  });
});
