import type { Config } from 'tailwindcss'
import plugin from 'tailwindcss/plugin'
import * as colors from 'tailwindcss/colors'
import * as defaultTheme from 'tailwindcss/defaultTheme'
import flattenColorPalette from 'tailwindcss/lib/util/flattenColorPalette'

import typographyPlugin from '@tailwindcss/typography'
import formsPlugin from '@tailwindcss/forms'
import aspectRatioPlugin from '@tailwindcss/aspect-ratio'
import headlessUiPlugin from '@headlessui/tailwindcss'

export default {
  content: ['./index.html', './src/**/*.vue', './src/**/*.js', './src/**/*.ts'],
  darkMode: 'class',
  theme: {
    fontFamily: {
      ...defaultTheme.fontFamily,
      sans: ['Inter', ...defaultTheme.fontFamily.sans],
      'sans-var': ['Inter var', ...defaultTheme.fontFamily.sans],
      display: [
        ['Inter var', ...defaultTheme.fontFamily.sans],
        {
          fontFeatureSettings: '"cv11", "ss01"',
          fontVariationSettings: '"opsz" 32',
        }
      ]
    },
    extend: {
      colors: {
        primary: colors.sky,
        secondary: colors.amber,
        current: 'currentColor',
        block: colors.gray[50],
        'block-dark': colors.gray[900],
      },
      fontSize: {
        xxs: '.7rem',
      },
      animation: {
        'fade-in': 'fade-in .3s ease',
        'fade-out': 'fade-out 3s ease 3s',
        'slide-in': 'slide-in .3s ease',
        'toast': 'fade-in .3s ease, fade-out 3s ease 3s, slide-in .3s ease',
      },
      keyframes: {
        'fade-in': {
          from: { opacity: '0' }
        },
        'fade-out': {
          to: { opacity: '0' }
        },
        'slide-in': {
          from: { transform: 'translateX(100%)' },
        }
      },
    },
  },
  plugins: [
    typographyPlugin,
    formsPlugin,
    aspectRatioPlugin,
    headlessUiPlugin,

    /** Base theme */
    plugin(function ({ addBase, addUtilities, theme }) {
      addBase({
        'html.dark': {
          colorScheme: 'dark',
        },
      })

      addUtilities({
        '.fancy-scrollbar': {
          /* Firefox */
          scrollbarWidth: 'thin',
          scrollbarColor: 'var(--scrollbar-thumb) var(--scrollbar-track)',
      
          /* Chrome, Edge, and Safari */
          '&::-webkit-scrollbar': {
            width: '10px',
          },
      
          '&::-webkit-scrollbar-track': {
            background: 'var(--scrollbar-track)',
          },
      
          '&::-webkit-scrollbar-thumb': {
            backgroundColor: 'var(--scrollbar-thumb)',
      
            '&:hover': {
              backgroundColor: 'var(--scrollbar-thumb-hover)',
            },
          }
        }
      })
    }),

    /** Custom variants */
    plugin(function ({ addVariant }) {
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
    }),

    /** Skeleton loading. */
    plugin(function ({ matchUtilities, theme }) {
      matchUtilities(
        { skeleton: (value) => ({ backgroundColor: value }) },
        { values: flattenColorPalette(theme('backgroundColor')), type: 'color' }
      )
    }),

    /** Custom Inter font due to self-hosted nature. */
    plugin(function ({ addBase, addUtilities, theme }) {
      addUtilities({
        '.font-sans-safe': {
          fontFamily: theme('fontFamily.sans'),
          '@supports (font-variation-settings: normal)': {
            fontFamily: theme('fontFamily.sans-var'),
          }
        },
        '.font-display-safe': {
          fontFamily: theme('fontFamily.sans'),
          fontFeatureSettings: '"cv11", "ss01"',
          fontVariationSettings: '"opsz" 32',
          '@supports (font-variation-settings: normal)': {
            fontFamily: theme('fontFamily.sans-var'),
          }
        }
      })

      // Add the Inter var font-face.
      addBase({
        '@font-face': {
          fontFamily: 'Inter var',
          fontWeight: '100 900',
          fontDisplay: 'swap',
          fontStyle: 'normal',
          src: [
            'url("/fonts/Inter.var.woff2?v=3.19") format("woff2-variations"), url("/fonts/Inter.var.woff2?v=3.19") format("woff2")',
            'url("/fonts/Inter.var.woff2?v=3.19") format("woff2") tech("variations")',
          ],
        },
      })

      addBase({
        '@font-face': {
          fontFamily: 'Inter var',
          fontWeight: '100 900',
          fontDisplay: 'swap',
          fontStyle: 'italic',
          src: [
            'url("/fonts/Inter.var.woff2?v=3.19") format("woff2-variations"), url("/fonts/Inter.var.woff2?v=3.19") format("woff2")',
            'url("/fonts/Inter.var.woff2?v=3.19") format("woff2") tech("variations")',
          ],
        },
      })

      const fonts = {
        '100': 'Thin',
        '200': 'ExtraLight',
        '300': 'Light',
        '400': 'Regular',
        '500': 'Medium',
        '600': 'SemiBold',
        '700': 'Bold',
        '800': 'ExtraBold',
        '900': 'Black',
      }

      for (const [weight, name] of Object.entries(fonts)) {
        addBase({
          '@font-face': {
            fontFamily: 'Inter',
            fontStyle: 'normal',
            fontWeight: weight,
            fontDisplay: 'swap',
            src: `url("/fonts/Inter-${name}.woff2?v=3.19") format("woff2")`,
          }
        })

        addBase({
          '@font-face': {
            fontFamily: 'Inter',
            fontStyle: 'italic',
            fontWeight: weight,
            fontDisplay: 'swap',
            src: `url("/fonts/Inter-${name}Italic.woff2?v=3.19") format("woff2")`,
          }
        })
      }
    }),
  ],
} satisfies Config
