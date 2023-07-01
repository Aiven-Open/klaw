// mock for the Light component
// for react-syntax-highlighter
// we're not testing the highlighting as
// it's css only

function Light({ children }: { children: string }) {
  return <div data-testid={"react-syntax-highlighter-mock"}>{children}</div>;
}

export { Light };
