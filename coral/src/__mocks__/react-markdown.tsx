/** ReactMarkdown users ES modules, which are not supported well
 * by jest. So we need a mock to enable testing.
 * The testid is added for convenient, we can query for
 * `xByTestId("react-markdown-mock") in tests to make
 * sure the right mock (and therefor the right component)
 * is used
 */

function ReactMarkdown({ children }: { children: string }) {
  return <div data-testid={"react-markdown-mock"}>{children}</div>;
}

export default ReactMarkdown;
