import { defineConfig } from 'vitepress'
import mdDeflist from 'markdown-it-deflist'
import mdImplicitFigures from 'markdown-it-image-figures'
import mdKbd from 'markdown-it-kbd'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  lang: 'en-US',
  title: 'Tankobon',
  description: 'Self-hosted physical book collection manager',

  base: '/tankobon/',

  srcDir: './src',

  lastUpdated: true,

  markdown: {
    config: (md) => {
      md.use(mdImplicitFigures, {
        lazy: true,
        figcaption: 'title',
      })

      md.use(mdDeflist)
      md.use(mdKbd)
    }
  },

  head: [
    ['link', {
      rel: 'icon',
      type: 'image/svg+xml',
      href: '/logo.svg',
    }],
  ],

  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    logo: '/logo.svg',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guides', link: '/guides/' }
    ],

    footer: {
      message: 'Released under the <a href="https://github.com/alessandrojean/tankobon/blob/main/LICENSE" target="_blank">MIT license</a>.',
      copyright: 'Copyright © 2023-present <a href="https://alessandrojean.github.io" target="_blank">Alessandro Jean</a>',
    },

    search: {
      provider: 'local',
    },

    editLink: {
      pattern: 'https://github.com/alessandrojean/tankobon/edit/main/docs/:path',
    },

    outline: [2, 3],

    sidebar: [
      {
        text: 'Installation',
        collapsed: true,
        items: [
          { text: 'Getting started', link: '/installation/' },
          { text: 'Run with Docker', link: '/installation/docker' },
          { text: 'Run with the Jar file', link: '/installation/jar' },
          { text: 'Accessing the web client', link: '/installation/webclient' },
          { text: 'Configuration', link: '/installation/configuration' },
        ]
      },
      {
        text: 'Guides',
        collapsed: true,
        items: [
          { text: 'Introduction', link: '/guides/' },
          { text: 'Searching', link: '/guides/search' },
          { text: 'REST API', link: '/guides/rest' },
        ]
      },
      {
        text: 'Entities',
        collapsed: true,
        items: [
          { text: 'Libraries', link: '/entities/libraries' },
          { text: 'Books', link: '/entities/books' },
          { text: 'Collections', link: '/entities/collections' },
          { text: 'Series', link: '/entities/series' },
          { text: 'Publishers', link: '/entities/publishers' },
          { text: 'Stores', link: '/entities/stores' },
          { text: 'People', link: '/entities/people' },
          { text: 'Roles', link: '/entities/contributor-roles' },
          { text: 'Tags', link: '/entities/tags' },
          { text: 'Users', link: '/entities/users' },
        ],
      },
      {
        text: 'Contributing',
        items: [
          { text: 'Overview', link: '/contributing/' },
        ],
      },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/alessandrojean/tankobon' }
    ],
  }
})
