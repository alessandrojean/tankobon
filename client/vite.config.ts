import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'
import childProcess from 'node:child_process'
import { defineConfig } from 'vite'
import Vue from '@vitejs/plugin-vue'
import Pages from 'vite-plugin-pages'
import Layouts from 'vite-plugin-vue-layouts'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import VueI18n from '@intlify/unplugin-vue-i18n/vite'
import { HeadlessUiResolver } from 'unplugin-vue-components/resolvers'
import { unheadVueComposablesImports } from '@unhead/vue'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const commitHash = childProcess
  .execSync('git rev-parse HEAD')
  .toString()
  .trim()

export default defineConfig({
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '~~': resolve(__dirname, '.'),
    },
  },

  define: {
    'import.meta.env.APP_VERSION': JSON.stringify(process.env.npm_package_version),
    'import.meta.env.GIT_SHORT_HASH': JSON.stringify(commitHash.slice(0, 7)),
    'import.meta.env.GIT_HASH': JSON.stringify(commitHash),
  },

  plugins: [
    // https://github.com/vitejs/vite/tree/main/packages/plugin-vue
    Vue({
      script: {
        defineModel: true,
      },
      template: {
        compilerOptions: {
          isCustomElement: tag => tag !== 'i18n-t' && tag.includes('-'),
        },
      },
    }),

    // https://github.com/hannoeru/vite-plugin-pages
    Pages(),

    // https://github.com/JohnCampionJr/vite-plugin-vue-layouts
    Layouts(),

    // https://github.com/antfu/unplugin-auto-import
    AutoImport({
      imports: [
        'vue',
        'vue-router',
        'vue-i18n',
        '@vueuse/core',
        '@vueuse/math',
        unheadVueComposablesImports,
        { '@vueuse/router': ['useRouteHash', 'useRouteParams', 'useRouteQuery'] },
        {
          from: '@vueuse/core',
          imports: ['MaybeRef'],
          type: true,
        },
        { cva: ['cva'] },
        { from: 'cva', imports: ['VariantProps'], type: true },
      ],
      dts: 'src/auto-imports.d.ts',
      dirs: [
        'src/composables/**',
        'src/stores',
        'src/queries/**',
        'src/mutations/**',
      ],
      vueTemplate: true,
      eslintrc: { enabled: true },
    }),

    // https://github.com/antfu/unplugin-vue-components
    Components({
      dts: 'src/components.d.ts',
      resolvers: [HeadlessUiResolver()],
    }),

    // https://github.com/intlify/bundle-tools/tree/main/packages/unplugin-vue-i18n
    VueI18n({
      runtimeOnly: true,
      compositionOnly: true,
      fullInstall: true,
      include: [resolve(__dirname, 'locales/**')],
    }),
  ],

  server: {
    port: 8081,
    proxy: {
      '/api': 'http://localhost:8080',
      '/images': 'http://localhost:8080',
    },
    watch: {
      ignored: ['**/tests/**'],
    },
  },

  test: {
    include: ['test/**/*.test.ts'],
    environment: 'jsdom',
    deps: {
      inline: ['@vue', '@vueuse', 'vue-demi'],
    },
  },
})
