import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'

import App from './App.vue'
import router from '@/router'
import { i18n } from '@/modules/i18n'

import '@github/markdown-toolbar-element'
import './index.pcss'

createApp(App)
  .use(createPinia())
  .use(VueQueryPlugin)
  .use(i18n)
  .use(router)
  .mount('#app')
