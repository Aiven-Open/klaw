import App from "./App";
import { render, screen } from "@testing-library/react";

describe("App.tsx", () => {
  it("shows a headline", () => {
    render(<App />);
    const heading = screen.getByRole("heading", { name: "Hello Klaw 👋" });

    expect(heading).toBeVisible();
  });
});
