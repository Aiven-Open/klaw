import { SchemaStats } from "src/app/features/topics/details/schema/components/SchemaStats";
import { cleanup, render, screen } from "@testing-library/react";

const testVersion = 99;
const testId = 111;
const testCompatibility = "BACKWARD";
describe("SchemaStats", () => {
  beforeAll(() => {
    render(
      <SchemaStats
        version={testVersion}
        id={testId}
        compatibility={testCompatibility}
      />
    );
  });

  afterAll(cleanup);

  it("shows the given version number", () => {
    const version = screen.getByText("Version no.");

    expect(version.parentElement).toHaveTextContent(`${testVersion}Version no`);
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
