const colors = require('tailwindcss/colors')
const defaultTheme = require('tailwindcss/defaultTheme')
const { default: flattenColorPalette } = require('tailwindcss/lib/util/flattenColorPalette')

const typographyPlugin = require('@tailwindcss/typography')
const formsPlugin = require('@tailwindcss/forms')
const aspectRatioPlugin = require('@tailwindcss/aspect-ratio')

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.vue', './src/**/*.js', './src/**/*.ts'],
  darkMode: 'class',
  theme: {
    fontFamily: {
      ...defaultTheme.fontFamily,
      sans: ['Inter', ...defaultTheme.fontFamily.sans],
      'sans-var': ['Inter var', ...defaultTheme.fontFamily.sans],
      display: ['Inter', ...defaultTheme.fontFamily.sans],
    },
    extend: {
      colors: {
        primary: colors.sky,
        secondary: colors.amber,
        current: 'currentColor',
        block: colors.gray[50],
        'block-dark': '#18212F',
      },
      fontSize: {
        xxs: '.7rem',
      },
    },
  },
  plugins: [
    typographyPlugin,
    formsPlugin,
    aspectRatioPlugin,
    function ({ addVariant }) {
      addVariant(
        'supports-backdrop-blur',
        '@supports (backdrop-filter: blur(0)) or (-webkit-backdrop-filter: blur(0))'
      )
      addVariant('hocus', ['&:hover', '&:focus-visible'])
      addVariant(
        'group-hocus',
        ':merge(.group):where(:hover, :focus-visible) &'
      )
      addVariant('not-disabled', '&:not(:disabled)')
      addVariant('group-not-disabled', ':merge(.group):not(:disabled) &')
      addVariant(
        'supports-var-font',
        '@supports (font-variation-settings: normal)'
      )
    },
    function ({ matchUtilities, theme }) {
      matchUtilities(
        { skeleton: (value) => ({ backgroundColor: value }) },
        { values: flattenColorPalette(theme('backgroundColor')), type: 'color' }
      )
    }
  ],
}
