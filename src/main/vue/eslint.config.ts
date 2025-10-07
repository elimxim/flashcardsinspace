import * as js from '@eslint/js'
import * as pluginVue from 'eslint-plugin-vue'
import * as tseslint from 'typescript-eslint'
import * as eslintConfigPrettier from 'eslint-config-prettier'
const prettierNs = eslintConfigPrettier as unknown as { default?: object }
const prettierConfig = (prettierNs.default ?? (eslintConfigPrettier as unknown as object))

export default [
  {
    linterOptions: {
      reportUnusedDisableDirectives: 'error',
    },
  },

  {
    ignores: ['node_modules/', 'dist/', 'build/', 'logs/', '.gradle/', '.idea/', '**/vite.config.js'],
  },

  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/recommended'],

  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
        extraFileExtensions: ['.vue'],
      },
    },
  },

  {
    rules: {
      'vue/multi-word-component-names': 'off',
    },
  },

  {
    files: ['**/*.vue'],
    rules: {
      'vue/multi-word-component-names': 'off',
      '@typescript-eslint/no-unused-vars': ['warn', {
        "args": "after-used",
        "argsIgnorePattern": "^_",
        "varsIgnorePattern": "^_",
        "ignoreRestSiblings": true
      }],
    },
  },

  {
    files: ['**/*.ts', '**/*.tsx', '**/*.vue'],
    rules: {
      'no-undef': 'off',
      'no-unused-vars': 'off',
    },
  },

  prettierConfig,

  {
    files: ['**/*.ts', '**/*.tsx', '**/*.vue'],
    rules: {
      'semi': ['error', 'never'],
    },
  },
]
