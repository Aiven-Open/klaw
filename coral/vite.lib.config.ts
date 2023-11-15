import react from "@vitejs/plugin-react";
import { resolve } from "path";
import { defineConfig } from "vite";
import svgr from "vite-plugin-svgr";
import dts from "vite-plugin-dts";
import { libInjectCss } from "vite-plugin-lib-inject-css";
export default defineConfig({
  plugins: [
    react(),
    svgr(),
    libInjectCss(),
    dts({
      tsconfigPath: "tsconfig.lib.json",
      beforeWriteFile: (filePath, content) => ({
        filePath: filePath.replace("lib-export.d.ts", "index.d.ts"),
        content,
      }),
    }),
  ],
  resolve: {
    alias: {
      src: resolve(resolve(__dirname), "./src"),
    },
  },
  build: {
    outDir: "lib-dist",
    target: "es2015",
    lib: {
      entry: resolve(__dirname, "lib-export.tsx"),
      name: "Coral lib",
      fileName: "index",
      formats: ["es", "umd", "cjs"],
    },
    rollupOptions: {
      input: "lib-export.tsx",
      external: ["react", "react-dom"],
      output: {
        globals: {
          react: "React",
          "react-dom": "ReactDOM",
        },
      },
    },
  },
});
