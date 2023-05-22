export interface AllowedHosts {
  name: string
  hosts: string[]
}

export const allowedHostsMap: Record<string, AllowedHosts> = {
  twitter: {
    name: 'Twitter',
    hosts: ['twitter.com', 'mobile.twitter.com'],
  },
  facebook: {
    name: 'Facebook',
    hosts: ['facebook.com'],
  },
  instagram: {
    name: 'Instagram',
    hosts: ['instagram.com'],
  },
  youTube: {
    name: 'YouTube',
    hosts: ['youtube.com'],
  },
  amazon: {
    name: 'Amazon',
    hosts: ['amazon.com', 'amazon.ca', 'amazon.com.br', 'amazon.co.uk', 'amazon.co.jp', 'amazon.cn', 'amazon.com.au', 'amazon.com.be', 'amazon.eg', 'amazon.fr', 'amazon.in', 'amazon.it', 'amazon.com.mx', 'amazon.nl', 'amazon.pl', 'amazon.sa', 'amazon.sg', 'amazon.es', 'amazon.se', 'amazon.com.tr', 'amazon.ae', 'amazon.de'],
  },
  openLibrary: {
    name: 'Open Library',
    hosts: ['openlibrary.org'],
  },
  skoob: {
    name: 'Skoob',
    hosts: ['skoob.com.br'],
  },
  goodreads: {
    name: 'Goodreads',
    hosts: ['goodreads.com'],
  },
  guiaDosQuadrinhos: {
    name: 'Guia dos Quadrinhos',
    hosts: ['guiadosquadrinhos.com'],
  },
  myAnimeList: {
    name: 'MyAnimeList',
    hosts: ['myanimelist.net'],
  },
  kitsu: {
    name: 'Kitsu',
    hosts: ['kitsu.io'],
  },
  aniList: {
    name: 'AniList',
    hosts: ['anilist.co'],
  },
  mangaUpdates: {
    name: 'MangaUpdates',
    hosts: ['mangaupdates.com'],
  },
}
