//ReactMarkdown users ES modules that jest still does not support well
function ReactMarkdown({ children }: { children: string }) {
  return <div data-testid={"react-markdown-mock"}>{children}</div>;
}

export default ReactMarkdown;
