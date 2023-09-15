// GitHub issue to investigate a more versatile testing approach:
// 🐙 https://github.com/aiven/klaw/issues/475
jest.mock("@monaco-editor/react", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@monaco-editor/react"),
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    //@ts-ignore
    default: (props) => {
      return (
        <textarea
          data-testid={props["data-testid"] ?? "mocked-monaco-editor"}
          value={props.value}
          onChange={(event) => props.onChange(event.target.value)}
        ></textarea>
      );
    },
  };
});
