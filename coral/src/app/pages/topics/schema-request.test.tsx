import { cleanup, screen } from "@testing-library/react/pure";
import SchemaRequest from "src/app/pages/topics/schema-request";
import { getQueryClientForTests } from "src/services/test-utils/query-client-tests";
import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { getEnvironmentsForSchemaRequest } from "src/domain/environment";
import { createSchemaRequest } from "src/domain/schema-request";
import { getTopicNames } from "src/domain/topic";

vi.mock("src/domain/schema-request/schema-request-api.ts");
vi.mock("src/domain/environment/environment-api.ts");
vi.mock("src/domain/topic/topic-api.ts");

const mockGetSchemaRegistryEnvironments =
  getEnvironmentsForSchemaRequest as vi.MockedFunction<
    typeof getEnvironmentsForSchemaRequest
  >;
const mockCreateSchemaRequest = createSchemaRequest as vi.MockedFunction<
  typeof createSchemaRequest
>;
const mockGetTopicNames = getTopicNames as vi.MockedFunction<
  typeof getTopicNames
>;

describe("SchemaRequest", () => {
  describe("renders the page to request a new schema for a topic", () => {
    const topicName = "my-awesome-topic";

    beforeAll(() => {
      mockGetSchemaRegistryEnvironments.mockResolvedValue([]);
      mockCreateSchemaRequest.mockImplementation(vi.fn());
      mockGetTopicNames.mockResolvedValue([topicName]);
      // @TODO if we decide to go with this kind of dynamic routes,
      // this should be enabled by customRender!
      const queryClient = getQueryClientForTests();
      render(
        <QueryClientProvider client={queryClient}>
          <MemoryRouter initialEntries={[`/topic/${topicName}/request-schema`]}>
            <Routes>
              <Route
                path={`/topic/:topicName/request-schema`}
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

    it("shows a headline to request a schema for a specific topic", () => {
      const headline = screen.getByRole("heading", {
        name: `Request new schema for topic "${topicName}"`,
      });

      expect(headline).toBeVisible();
    });

    it("shows a form for to request a schema", () => {
      const form = screen.getByRole("form", {
        name: `Request a new schema`,
      });

      expect(form).toBeVisible();
    });

    it("shows the form for this specific topic", async () => {
      const input = await screen.findByRole("combobox", {
        name: "Topic name",
      });

      expect(input).toBeVisible();
      expect(input).toHaveValue(topicName);
      expect(input).toHaveAttribute("aria-readonly", "true");
    });
  });
});
