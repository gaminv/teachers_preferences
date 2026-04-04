import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    setupFiles: "./src/tests/setupTests.js",
    globals: true,
    css: true,
    exclude: [
      "e2e/**",
      "playwright.config.js",
      "node_modules/**",
      "dist/**"
    ],
    coverage: {
      provider: "v8",
      reporter: ["text", "html", "json-summary"],
      reportsDirectory: "./coverage",
      include: ["src/**/*.{js,jsx,ts,tsx}"],
      exclude: [
        "src/main.jsx",
        "src/tests/**",
        "**/*.d.ts"
      ]
    }
  }
});
