import js from "@eslint/js";
import globals from "globals";
import tseslint from "typescript-eslint";

export default tseslint.config(
  { ignores: ["reports", "test-results", ".auth"] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ["**/*.ts"],
    languageOptions: { ecmaVersion: 2022, globals: globals.node },
    rules: {
      // Playwright fixtures declare their dependencies by destructuring the first
      // parameter, so a fixture that needs nothing is written `async ({}, use)`
      "no-empty-pattern": ["error", { allowObjectPatternsAsParameters: true }],
    },
  },
);
