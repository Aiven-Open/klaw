import { defineConfig } from "tsup";

export default defineConfig({
  dts: true,
  bundle: true,
  treeshake: true,
  sourcemap: true,
  format: ["esm", "cjs"],
  entry: ["./index.ts"],
  loader: {
    ".tsx": "tsx",
  },
  shims: true,
  outExtension({ format }) {
    return {
      js: `.${format}.tsx`,
    };
  },
});
