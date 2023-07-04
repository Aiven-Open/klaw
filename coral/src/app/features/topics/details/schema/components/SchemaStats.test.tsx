import { SchemaStats } from "src/app/features/topics/details/schema/components/SchemaStats";
import { cleanup, render, screen } from "@testing-library/react";

const testVersion = 99;
const testId = 111;
const testCompatibility = "BACKWARD";
describe("SchemaStats", () => {
  describe("renders a loading state", () => {
    render(
      <SchemaStats
        isLoading={true}
        version={testVersion}
        id={testId}
        compatibility={testCompatibility}
      />
    );

    afterAll(cleanup);

    it("shows loading information", () => {
      const loadingInformation = screen.getAllByText("Loading information");

      expect(loadingInformation).toHaveLength(3);
      loadingInformation.forEach((element) => {
        expect(element).toBeVisible();
        expect(element).toHaveClass("visually-hidden");
      });
    });

    it("shows no data for version number", () => {
      const versionStat = screen.queryByText(testVersion);

      expect(versionStat).not.toBeInTheDocument();
    });

    it("shows no data for ID", () => {
      const idStat = screen.queryByText(testId);

      expect(idStat).not.toBeInTheDocument();
    });

    it("shows no data for compatibility", () => {
      const compatibilityStat = screen.queryByText(testCompatibility);

      expect(compatibilityStat).not.toBeInTheDocument();
    });
  });

  describe("renders all necessary data", () => {
    beforeAll(() => {
      render(
        <SchemaStats
          isLoading={false}
          version={testVersion}
          id={testId}
          compatibility={testCompatibility}
        />
      );
    });

    afterAll(cleanup);

    it("shows the given version number", () => {
      const version = screen.getByText("Version no.");

      expect(version.parentElement).toHaveTextContent(
        `${testVersion}Version no`
      );
    });

    it("shows the given ID", () => {
      const id = screen.getByText("ID");

      expect(id.parentElement).toHaveTextContent(`${testId}ID`);
    });

    it("shows the given compatibility info", () => {
      const compatibility = screen.getByText("Compatibility");

      expect(compatibility.parentElement).toHaveTextContent(
        `${testCompatibility}Compatibility`
      );
    });
  });
});
