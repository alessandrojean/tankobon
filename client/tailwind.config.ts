import type { Config } from 'tailwindcss'
import plugin from 'tailwindcss/plugin'
import colors from 'tailwindcss/colors'
import defaultTheme from 'tailwindcss/defaultTheme'
import flattenColorPalette from 'tailwindcss/lib/util/flattenColorPalette'

import typographyPlugin from '@tailwindcss/typography'
import formsPlugin from '@tailwindcss/forms'
import aspectRatioPlugin from '@tailwindcss/aspect-ratio'
import containerQueriesPlugin from '@tailwindcss/container-queries'
import headlessUiPlugin from '@headlessui/tailwindcss'
import type { PluginUtils } from 'tailwindcss/types/config'

export default {
  content: ['./index.html', './src/**/*.vue', './src/**/*.js', './src/**/*.ts'],
  darkMode: 'class',
  theme: {
    fontFamily: {
      ...defaultTheme.fontFamily,
      'sans': ['Inter', ...defaultTheme.fontFamily.sans],
      'sans-var': ['\'Inter var\'', ...defaultTheme.fontFamily.sans],
      'display': [
        ['\'Inter var\'', ...defaultTheme.fontFamily.sans],
        {
          fontFeatureSettings: '"cv08", "cv11", "ss01"',
          fontVariationSettings: '"opsz" 32',
        },
      ],
      'japanese': [
        '\'ヒラギノ角ゴ Pro W3\'',
        '\'Hiragino Kaku Gothic Pro\'',
        '\'メイリオ\'',
        'Meiryo',
        '\'ＭＳ Ｐゴシック\'',
        '\'Noto Sans CJK JP\'',
        'Inter',
        ...defaultTheme.fontFamily.sans,
      ],
      'korean': [
        '\'Apple SD Gothic Neo\'',
        '\'NanumBarunGothic\'',
        '\'맑은 고딕\'',
        '\'Malgun Gothic\'',
        '\'굴림\'',
        '\'Gulim\'',
        '\'돋움\'',
        '\'Dotum\'',
        '\'Noto Sans CJK KR\'',
        'Inter',
        ...defaultTheme.fontFamily.sans,
      ],
      'chinese': [
        '\'华文细黑\'',
        '\'STXihei\'',
        '\'PingFang TC\'',
        '\'微软雅黑体\'',
        '\'Microsoft YaHei New\'',
        '\'微软雅黑\'',
        '\'Microsoft Yahei\'',
        '\'宋体\'',
        '\'SimSun\'',
        '\'Noto Sans CJK SC\'',
        'Inter',
        ...defaultTheme.fontFamily.sans,
      ],
    },
    extend: {
      colors: {
        'primary': colors.sky,
        'secondary': colors.amber,
        'current': 'currentColor',
        'block': colors.gray[50],
        'block-dark': colors.gray[900],
      },
      boxShadow: {
        'primer-overlay': 'rgba(27, 31, 36, 0.12) 0px 1px 3px, rgba(66, 74, 83, 0.12) 0px 8px 24px',
        'primer-overlay-dark': 'rgba(22, 25, 29, 0.12) 0px 1px 3px, rgba(56, 61, 63, 0.12) 0px 8px 24px',
      },
      fontSize: {
        xxs: '.7rem',
      },
      animation: {
        'fade-in': 'fade-in .3s ease',
        'fade-out': 'fade-out 3s ease 3s',
        'slide-in': 'slide-in .3s ease',
        'slide-in-neg': 'slide-in-neg .3s ease',
        'toast': 'fade-in .3s ease, fade-out 3s ease 3s, slide-in .3s ease',
      },
      transitionTimingFunction: {
        'in-out-primer': 'cubic-bezier(0.33, 1, 0.68, 1)',
      },
      keyframes: {
        'fade-in': {
          from: { opacity: '0' },
        },
        'fade-out': {
          to: { opacity: '0' },
        },
        'slide-in': {
          from: { transform: 'translateX(100%)' },
        },
        'slide-in-neg': {
          from: { transform: 'translateX(-100%)' },
        },
      },
      typography: ({ theme }: PluginUtils) => ({
        DEFAULT: {
          css: {
            '.video-player': {
              'position': 'relative',
              'maxWidth': '100%',
              'paddingTop': theme('padding.4'),
              'paddingBottom': theme('padding.4'),
              'borderRadius': theme('borderRadius.xl'),
              'boxShadow': theme('boxShadow.lg'),
              'backgroundColor': theme('colors.gray.50'),
              'overflow': 'hidden',

              '& > iframe': {
                position: 'absolute',
                width: '100%',
                height: '100%',
                top: '0px',
                left: '0px',
              },
            },
          },
        },
        invert: {
          css: {
            '.video-player': {
              backgroundColor: theme('colors.gray.800'),
            },
          },
        },
      }),
    },
  },
  plugins: [
    typographyPlugin,
    formsPlugin,
    aspectRatioPlugin,
    containerQueriesPlugin,
    headlessUiPlugin,

    /** Base theme */
    plugin(({ addBase, addUtilities }) => {
      addBase({
        'html.dark': {
          colorScheme: 'dark',
        },
      })

      addUtilities({
        '.fancy-scrollbar': {
          /* Firefox */
          'scrollbarWidth': 'thin',
          'scrollbarColor': 'var(--scrollbar-thumb) var(--scrollbar-track)',

          /* Chrome, Edge, and Safari */
          '&::-webkit-scrollbar': {
            width: '10px',
          },

          '&::-webkit-scrollbar-track': {
            background: 'var(--scrollbar-track)',
          },

          '&::-webkit-scrollbar-thumb': {
            'backgroundColor': 'var(--scrollbar-thumb)',

            '&:hover': {
              backgroundColor: 'var(--scrollbar-thumb-hover)',
            },
          },
        },
      })
    }),

    /** Custom variants */
    plugin(({ addVariant, matchVariant }) => {
      addVariant('light', 'html:not(.dark) &')
      addVariant('hocus', ['&:hover', '&:focus-visible'])
      addVariant('not-disabled', '&:not(:disabled)')

      matchVariant('group-hocus', (_, { modifier }) => {
        return modifier
          ? `:merge(.group\\/${modifier}):where(:hover, :focus-visible) &`
          : ':merge(.group):where(:hover, :focus-visible) &'
      }, { values: { DEFAULT: '' } })

      matchVariant('group-not-disabled', (_, { modifier }) => {
        return modifier
          ? `:merge(.group\\/${modifier}):not(:disabled) &`
          : ':merge(.group):not(:disabled) &'
      }, { values: { DEFAULT: '' } })
    }),

    /** Skeleton loading. */
    plugin(({ matchUtilities, theme }) => {
      matchUtilities(
        { skeleton: value => ({ backgroundColor: value }) },
        { values: flattenColorPalette(theme('backgroundColor')), type: 'color' },
      )
    }),

    /** Custom Inter font due to self-hosted nature. */
    plugin(({ addBase, addUtilities, theme }) => {
      addUtilities({
        '.font-sans-safe': {
          'fontFamily': theme('fontFamily.sans'),
          'fontFeatureSettings': 'initial',
          'fontVariationSettings': 'initial',
          '@supports (font-variation-settings: normal)': {
            fontFamily: theme('fontFamily.sans-var'),
            fontFeatureSettings: 'initial',
            fontVariationSettings: 'initial',
          },
        },
        '.font-display-safe': {
          'fontFamily': theme('fontFamily.sans'),
          'fontFeatureSettings': '"cv08", "cv11", "ss01"',
          'fontVariationSettings': '"opsz" 32',
          '@supports (font-variation-settings: normal)': {
            fontFamily: theme('fontFamily.sans-var'),
            fontFeatureSettings: '"cv08", "cv11", "ss01"',
            fontVariationSettings: '"opsz" 32',
          },
        },
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
            'url("/fonts/Inter-Italic.var.woff2?v=3.19") format("woff2-variations"), url("/fonts/Inter-Italic.var.woff2?v=3.19") format("woff2")',
            'url("/fonts/Inter-Italic.var.woff2?v=3.19") format("woff2") tech("variations")',
          ],
        },
      })

      const fonts = {
        100: 'Thin',
        200: 'ExtraLight',
        300: 'Light',
        400: 'Regular',
        500: 'Medium',
        600: 'SemiBold',
        700: 'Bold',
        800: 'ExtraBold',
        900: 'Black',
      }

      for (const [weight, name] of Object.entries(fonts)) {
        addBase({
          '@font-face': {
            fontFamily: 'Inter',
            fontStyle: 'normal',
            fontWeight: weight,
            fontDisplay: 'swap',
            src: `url("/fonts/Inter-${name}.woff2?v=3.19") format("woff2")`,
          },
        })

        addBase({
          '@font-face': {
            fontFamily: 'Inter',
            fontStyle: 'italic',
            fontWeight: weight,
            fontDisplay: 'swap',
            src: `url("/fonts/Inter-${name}Italic.woff2?v=3.19") format("woff2")`,
          },
        })
      }
    }),

    /** Custom fonts for different languages */
    // https://tech.busuu.com/handling-css-font-stacks-for-multi-language-websites-cf852e321f06
    plugin(({ addBase, theme }) => {
      addBase({
        '[lang|=ja]:not([lang*=Latn])': {
          fontFamily: `${theme('fontFamily.japanese')} !important`,
        },
        '[lang|=ko]:not([lang*=Latn])': {
          fontFamily: `${theme('fontFamily.korean')} !important`,
        },
        '[lang|=zh]:not([lang*=Latn])': {
          fontFamily: `${theme('fontFamily.chinese')} !important`,
        },
      })
    }),
  ],
} satisfies Config
