// eslint-disable-next-line @typescript-eslint/no-unused-vars
import React from "react";

jest.mock("@monaco-editor/react", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@monaco-editor/react"),
    default: jest.fn(({ onChange, ...rest }) => {
      return (
        <textarea
          data-testid={rest["data-testid"] ?? "mocked-monaco-editor"}
          onChange={(event) => onChange(event.target.value)}
        ></textarea>
      );
    }),
  };
});
