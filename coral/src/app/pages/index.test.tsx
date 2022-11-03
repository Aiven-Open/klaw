import HomePage from "src/app/pages";
import { screen } from "@testing-library/react";
import { renderWithQueryClient } from "src/services/test-utils";
import { server } from "src/domain/api-mocks/server";

const loadingText = "data is loading";
const userName = "Super Admin";

describe("HomePage", () => {
  beforeAll(() => {
    server.listen();
  });

  beforeEach(() => {
    // Note: As long as we're using a msw mock in the component
    // we can't use it in the test directly but set the "window.msw"
    // object to "server". This will call the mocked function
    // with server instead of worker
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    //@ts-ignore
    window.msw = server;
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
