import { cleanup, screen } from "@testing-library/react/pure";
import SchemaRequest from "src/app/pages/topics/schema-request";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { getAllEnvironmentsForTopicAndAcl } from "src/domain/environment";
import { requestSchemaCreation } from "src/domain/schema-request";
import { getTopicNames } from "src/domain/topic";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/schema-request/schema-request-api.ts");
jest.mock("src/domain/environment/environment-api.ts");
jest.mock("src/domain/topic/topic-api.ts");

const mockGetAllEnvironmentsForTopicAndAcl =
  getAllEnvironmentsForTopicAndAcl as jest.MockedFunction<
    typeof getAllEnvironmentsForTopicAndAcl
  >;
const mockCreateSchemaRequest = requestSchemaCreation as jest.MockedFunction<
  typeof requestSchemaCreation
>;
const mockGetTopicNames = getTopicNames as jest.MockedFunction<
  typeof getTopicNames
>;

describe("SchemaRequest", () => {
  describe("renders the page to request a new schema for a topic", () => {
    const topicName = "my-awesome-topic";

    beforeAll(() => {
      mockGetAllEnvironmentsForTopicAndAcl.mockResolvedValue([]);
      mockCreateSchemaRequest.mockImplementation(jest.fn());
      mockGetTopicNames.mockResolvedValue([topicName]);

      customRender(
        <MemoryRouter initialEntries={[`/topic/${topicName}/request-schema`]}>
          <Routes>
            <Route
              path={`/topic/:topicName/request-schema`}
              element={<SchemaRequest />}
            />
          </Routes>
        </MemoryRouter>,
        { queryClient: true, aquariumContext: true }
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
        name: "Topic name (read-only)",
      });

      expect(input).toBeVisible();
      expect(input).toHaveValue(topicName);
      expect(input).toHaveAttribute("aria-readonly", "true");
    });
  });
});
