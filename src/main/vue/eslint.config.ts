import * as js from '@eslint/js';
import * as pluginVue from 'eslint-plugin-vue';
import * as tseslint from 'typescript-eslint';
import * as eslintConfigPrettier from 'eslint-config-prettier';
// Normalize CJS/ESM interop for eslint-config-prettier (handles CJS default export)
const prettierNs = eslintConfigPrettier as unknown as { default?: object };
const prettierConfig = (prettierNs.default ?? (eslintConfigPrettier as unknown as object));

// Use flat config array (tseslint.config helper is deprecated)
export default [
  // Global configuration
  {
    linterOptions: {
      reportUnusedDisableDirectives: 'error',
    },
  },

  // Global ignores
  {
    ignores: ['node_modules/', 'dist/', 'build/', 'logs/', '.gradle/', '.idea/', '**/vite.config.js'],
  },

  // Base configs
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/recommended'],

  // Specific config for Vue files to use TypeScript parser
  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
        extraFileExtensions: ['.vue'],
      },
    },
  },

  // Custom rule overrides (global)
  {
    rules: {
      'vue/multi-word-component-names': 'off',
    },
  },

  // Ensure Vue-specific overrides come last so they take precedence over plugin defaults
  {
    files: ['**/*.vue'],
    rules: {
      // Allow single-word names in components
      'vue/multi-word-component-names': 'off',
      // Be pragmatic with unused checks inside SFCs; allow underscore-prefix to intentionally ignore
      '@typescript-eslint/no-unused-vars': ['warn', {
        "args": "after-used",
        "argsIgnorePattern": "^_",
        "varsIgnorePattern": "^_",
        "ignoreRestSiblings": true
      }],
    },
  },

  // Disable core rules that conflict/duplicate TS-aware ones in TS and Vue files
  {
    files: ['**/*.ts', '**/*.tsx', '**/*.vue'],
    rules: {
      'no-undef': 'off',
      'no-unused-vars': 'off',
    },
  },

  // Prettier config must be the last to override other formatting rules
  prettierConfig,
];
