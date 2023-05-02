import { fileURLToPath } from 'node:url'
import { resolve, dirname } from 'node:path'
import { defineConfig } from 'vite'
import Vue from '@vitejs/plugin-vue'
import Pages from 'vite-plugin-pages'
import Layouts from 'vite-plugin-vue-layouts'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import VueI18n from '@intlify/unplugin-vue-i18n/vite'
import postcss from './postcss.config.js'

import { HeadlessUiResolver } from 'unplugin-vue-components/resolvers'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

export default defineConfig({
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '~~': resolve(__dirname, '.'),
    },
  },

  css: { postcss },

  plugins: [
    // https://github.com/vitejs/vite/tree/main/packages/plugin-vue
    Vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag) => tag === 'markdown-toolbar' || tag.substring(0, 3) === 'md-'
        }
      }
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
        '@vueuse/head',
        '@vueuse/core',
        { '@vueuse/router': ['useRouteHash', 'useRouteParams', 'useRouteQuery'] },
        {
          from: '@vueuse/core',
          imports: ['MaybeRef'],
          type: true,
        },
        { 'cva': ['cva'] },
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
      resolvers: [HeadlessUiResolver()]
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
      ignored: ['**/src/tests/**'],
    },
  },
})
