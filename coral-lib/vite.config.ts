import react from "@vitejs/plugin-react";
import { resolve } from 'path'
import {defineConfig} from 'vite'
import svgr from "vite-plugin-svgr";
import {dts} from "rollup-plugin-dts";


export default defineConfig({
  plugins: [react(), svgr()],
  resolve: {
    alias: {
      // this makes sure that components imported in coral are
      // correctly resolved.
      'src': resolve(resolve(__dirname), "../coral/src"),
    },
  },
  build: {
    target: "es2015",
    lib: {
      entry: resolve(__dirname, 'components/index.tsx'),
      name: 'Coral lib',
      fileName: 'index',
      formats: ["es", "umd", "cjs"]
    },
    rollupOptions: {
      external: ['react', 'react-dom'],
      output: {
        globals: {
          react: 'React',
          'react-dom': 'ReactDOM'
        },
      },
      plugins: [dts()],
    },
  },
})