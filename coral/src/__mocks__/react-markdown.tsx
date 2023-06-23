jest.mock("rehype-raw");
// jest.mock("hast-util-raw");

function ReactMarkdown({ children }: { children: string }) {
  return <div data-testid={"react-markdown-mock"}>{children}</div>;
}

export default ReactMarkdown;
