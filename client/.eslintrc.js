module.exports = {
  root: true,
  plugins: ['prettier'],
  env: {
    browser: true,
    node: true
  },
  ignorePatterns: ['src/auto-imports.d.ts', 'src/components.d.ts'],
  extends: [
    'plugin:vue/vue3-essential',
    'prettier',
    'plugin:storybook/recommended',
    './.eslintrc-auto-import.json'
  ],
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@typescript-eslint/parser'
  },
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'vue/multi-word-component-names': 'off',
    'prettier/prettier': [
      'warn',
      {
        semi: false,
        printWidth: 80,
        singleQuote: true,
        trailingComma: 'none'
      }
    ]
  }
}
