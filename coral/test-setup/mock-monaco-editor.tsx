// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import React from "react"; // eslint-disable-line @typescript-eslint/no-unused-vars
import { vi } from "vitest";

// github issue to investigate a more versatile testing approach:
// 🐙 https://github.com/aiven/klaw/issues/475
vi.mock("@monaco-editor/react", () => {
  return {
    __esModule: true,
    ...vi.importActual("@monaco-editor/react"),
    default: vi.fn((props) => {
      return (
        <textarea
          data-testid={props["data-testid"] ?? "mocked-monaco-editor"}
          value={props.value}
          onChange={(event) => props.onChange(event.target.value)}
        ></textarea>
      );
    }),
  };
});
