import { cleanup, screen } from "@testing-library/react/pure";
import SchemaRequest from "src/app/pages/topics/schema-request";
import { getQueryClientForTests } from "src/services/test-utils/query-client-tests";
import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, Routes, Route } from "react-router-dom";

// mock out svgs to avoid clutter
jest.mock("@aivenio/aquarium", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/aquarium"),
    Icon: () => null,
  };
});

describe("SchemaRequest", () => {
  describe("renders the page to request a new schema for a topic", () => {
    const topicName = "my-awesome-topic";

    beforeAll(() => {
      // @TODO if we decide to go with this kind of dynamic routes,
      // this should be enabled by customRender!
      const queryClient = getQueryClientForTests();
      render(
        <QueryClientProvider client={queryClient}>
          <MemoryRouter initialEntries={[`/topic/${topicName}/request/schema`]}>
            <Routes>
              <Route
                path={`/topic/:topicName/request/schema`}
                element={<SchemaRequest />}
              />
            </Routes>
          </MemoryRouter>
        </QueryClientProvider>
      );
    });

    afterAll(() => {
      cleanup();
    });

    it("shows a headline to request a schema for a specific topic", async () => {
      const headline = await screen.findByRole("heading", {
        name: `Request new schema for topic "${topicName}"`,
      });

      expect(headline).toBeVisible();
    });
  });
});
