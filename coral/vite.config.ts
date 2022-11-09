import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { resolve } from "path";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // Vite does not use process.env (see https://vitejs.dev/guide/env-and-mode.html).
    // If a library depends on process.env (like "@aivenio/design-system"),
    // the needed env variable can be set here like with EXAMPLE.
    // ⛔ Note: there are stackoverflow answers / github issues that recommend e.g
    // ⛔ 'process.env': process.env or
    // ⛔ 'process.env': { ...process.env}
    // ⛔️ Don't do that! This can expose unwanted env vars in production builds.
    "process.env": {
      EXAMPLE: "",
    },
  },
  css: {
    modules: {
      localsConvention: "camelCaseOnly",
    },
  },
  resolve: {
    alias: {
      src: resolve(resolve(__dirname), "./src"),
    },
  },
});
