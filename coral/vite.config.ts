import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import { resolve } from "path";

/**
 * Get basename for React router.
 *
 * @param  {[Record<string, string>]} environment
 *
 * Picks first encountered value:
 * 1. $VITE_ROUTER_BASENAME
 * 2. $BASE_URL
 * 3. undefined (default)
 */
function getRouterBasename(env: Record<string, string>): string | undefined {
  return env.VITE_ROUTER_BASENAME ?? env.BASE_URL ?? undefined;
}

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const environment = loadEnv(mode, process.cwd(), "");
  return {
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
        ROUTER_BASENAME: getRouterBasename(environment),
      },
    },
    resolve: {
      alias: {
        src: resolve(resolve(__dirname), "./src"),
      },
    },
  };
});
