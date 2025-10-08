/*
 * For a detailed explanation regarding each configuration property and type check, visit:
 * https://jestjs.io/docs/configuration
 */
const esModules = ["@aivenio/aquarium", "d3", "internmap", "lodash-es"].join(
  "|"
);

//eslint-disable-next-line
export default {
  moduleFileExtensions: ["js", "jsx", "ts", "tsx"],
  preset: "ts-jest",
  testEnvironment: "jsdom",
  setupFiles: ["@testing-library/react/dont-cleanup-after-each"],
  setupFilesAfterEnv: [
    "<rootDir>/test-setup/setup-files-after-env.ts",
    "<rootDir>/test-setup/mock-monaco-editor.tsx",
    "<rootDir>/test-setup/mock-ds-icon-component.tsx",
    "<rootDir>/test-setup/mock-ds-tabBadge-component.tsx",
  ],
  transform: {
    "^.+\\.[tj]sx?|mjs$": [
      "ts-jest",
      { tsconfig: "<rootDir>/tsconfig.jest.json" },
    ],
  },
  transformIgnorePatterns: [
    `/node_modules/(?!.*(${esModules}))/`,
    "^.+\\.json$",
  ],
  moduleNameMapper: {
    ".+\\.(png|jpg|ttf|woff|woff2)$": "jest-transform-stub",
    ".+\\.svg$": "<rootDir>/src/__mocks__/svg.ts",
    "\\.css$": "identity-obj-proxy",
    "^src/(.*)$": "<rootDir>/src/$1",
  },
  // Fixing "Cannot find module ‘msw/node’"
  // https://mswjs.io/docs/migrations/1.x-to-2.x#cannot-find-module-mswnode-jsdom
  testEnvironmentOptions: {
    customExportConditions: [""],
  },
};
