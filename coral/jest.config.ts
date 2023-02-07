/*
 * For a detailed explanation regarding each configuration property and type check, visit:
 * https://jestjs.io/docs/configuration
 */

//eslint-disable-next-line
export default {
  moduleFileExtensions: ["js", "jsx", "ts", "tsx"],
  preset: "ts-jest",
  testEnvironment: "jsdom",
  setupFiles: ["@testing-library/react/dont-cleanup-after-each"],
  setupFilesAfterEnv: [
    "<rootDir>/test-setup/setup-files-after-env.ts",
    "<rootDir>/test-setup/mock-monaco-editor.tsx",
  ],

  moduleNameMapper: {
    ".+\\.(png|jpg|ttf|woff|woff2|svg)$": "jest-transform-stub",
    "\\.css$": "identity-obj-proxy",
    "^src/(.*)$": "<rootDir>/src/$1",
  },
};
