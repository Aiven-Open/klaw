import { cleanup, render, screen } from "@testing-library/react";
import PreviewBanner from "src/app/components/PreviewBanner";

describe("PreviewBanner.tsx", () => {
  beforeAll(() => {
    render(<PreviewBanner linkTarget={"/browseTopics"} />);
  });
  afterAll(() => {
    cleanup();
  });
  it("has an accessible copy explaining that user is viewing preview UI", () => {
    const region = screen.getByRole("region", { name: "Preview disclaimer" });

    expect(region).toBeVisible();
    expect(region).toHaveTextContent(
      /You are viewing a preview of the redesigned user interface./i
    );
  });

  it("has link that allows early adopters to give feedback", () => {
    const feedbackLink = screen.getByRole("link", { name: "feedback" });
    expect(feedbackLink).toHaveAttribute(
      "href",
      "https://github.com/aiven/klaw/issues/new?template=03_feature.md"
    );
    expect(feedbackLink).toHaveAttribute("target", "_blank");
    expect(feedbackLink).toHaveAttribute("rel", "noreferrer");
  });

  it("allows user to navigate back to old interface", () => {
    const backToOldLink = screen.getByRole("link", { name: "old interface" });
    expect(backToOldLink).toHaveAttribute("href", "/browseTopics");
  });
});
