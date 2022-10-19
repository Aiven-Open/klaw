/*
 * For a detailed explanation regarding each configuration property and type check, visit:
 * https://jestjs.io/docs/configuration
 */

export default {
  moduleFileExtensions: ["js", "jsx", "ts", "tsx"],
  preset: "ts-jest",
  testEnvironment: "jsdom",
  setupFilesAfterEnv: ['<rootDir>/test-setup/setup-files-after-env.ts'],
  moduleNameMapper: {
    ".+\\.(css|styl|less|sass|scss|png|jpg|ttf|woff|woff2|svg)$":
        "jest-transform-stub",
    "^@/(.*)$": "<rootDir>/src/$1",
  },
};
