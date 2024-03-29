<script setup lang="ts">
import AniListIcon from '@/components/icons/AniListIcon.vue'
import InstagramIcon from '@/components/icons/InstagramIcon.vue'
import KitsuIcon from '@/components/icons/KitsuIcon.vue'
import MyAnimeListIcon from '@/components/icons/MyAnimeListIcon.vue'
import TwitterIcon from '@/components/icons/TwitterIcon.vue'
import UnknownWebsiteIcon from '@/components/icons/UnknownWebsiteIcon.vue'
import WebsiteIcon from '@/components/icons/WebsiteIcon.vue'
import AmazonIcon from '@/components/icons/AmazonIcon.vue'
import OpenLibraryIcon from '@/components/icons/OpenLibraryIcon.vue'
import GoodreadsIcon from '@/components/icons/GoodreadsIcon.vue'
import SkoobIcon from '@/components/icons/SkoobIcon.vue'
import GuiaDosQuadrinhosIcon from '@/components/icons/GuiaDosQuadrinhosIcon.vue'
import MangaUpdatesIcon from '@/components/icons/MangaUpdatesIcon.vue'
import FacebookIcon from '@/components/icons/FacebookIcon.vue'
import YouTubeIcon from '@/components/icons/YouTubeIcon.vue'
import StoreIcon from '@/components/icons/StoreIcon.vue'
import PixivIcon from '@/components/icons/PixivIcon.vue'
import DeviantArtIcon from '@/components/icons/DeviantArtIcon.vue'
import type { FormExternalLink } from '@/types/tankobon-external-link'

export interface EntityExternalLinksProps {
  links?: Record<string, string | null>
  loading?: boolean
}

const props = withDefaults(defineProps<EntityExternalLinksProps>(), {
  loading: false,
})

const { links } = toRefs(props)
const { t, locale } = useI18n()

const typeNames = computed<Record<string, string>>(() => ({
  null: t('external-links.type-unknown'),
  website: t('external-links.website'),
  store: t('external-links.store'),
  myAnimeList: 'MyAnimeList',
  kitsu: 'Kitsu',
  aniList: 'AniList',
  twitter: 'Twitter',
  instagram: 'Instagram',
  facebook: 'Facebook',
  youTube: 'YouTube',
  amazon: 'Amazon',
  openLibrary: 'Open Library',
  goodreads: 'Goodreads',
  skoob: 'Skoob',
  mangaUpdates: 'MangaUpdates',
  guiaDosQuadrinhos: 'Guia dos Quadrinhos',
  deviantArt: 'DeviantArt',
  pixiv: 'Pixiv',
}))

const typeIcons: Record<string, Component> = {
  null: UnknownWebsiteIcon,
  website: WebsiteIcon,
  store: StoreIcon,
  myAnimeList: MyAnimeListIcon,
  kitsu: KitsuIcon,
  aniList: AniListIcon,
  twitter: TwitterIcon,
  instagram: InstagramIcon,
  facebook: FacebookIcon,
  youTube: YouTubeIcon,
  amazon: AmazonIcon,
  openLibrary: OpenLibraryIcon,
  goodreads: GoodreadsIcon,
  skoob: SkoobIcon,
  mangaUpdates: MangaUpdatesIcon,
  guiaDosQuadrinhos: GuiaDosQuadrinhosIcon,
  pixiv: PixivIcon,
  deviantArt: DeviantArtIcon,
}

const presentLinks = computed(() => {
  return Object.entries(links?.value ?? {})
    .map(([type, url]) => ({ type, url }))
    .filter(({ url }) => url !== null)
    .sort((a, b) => {
      return typeNames.value[a.type]
        ?.localeCompare(typeNames.value[b.type], locale.value)
    }) as FormExternalLink[]
})
</script>

<template>
  <Block v-if="presentLinks.length || loading" :title="$t('external-links.title')">
    <div v-if="loading" class="flex flex-wrap gap-2">
      <div v-for="i in 4" :key="i" class="skeleton w-32 h-9" />
    </div>
    <ul v-else-if="presentLinks.length" class="flex flex-wrap gap-2">
      <li
        v-for="link in presentLinks"
        :key="link.type"
      >
        <a
          target="_blank"
          rel="noopener"
          :href="link.url"
          :title="typeNames[link.type]"
          :class="[
            'flex items-center gap-2.5 pl-1.5 pr-3 py-1.5 rounded-lg text-sm',
            'bg-gray-200/90 dark:bg-gray-700/60',
            'hover:bg-gray-300 dark:hover:bg-gray-600/70',
            'focus:outline-none focus:ring-2 focus:ring-black dark:focus:ring-white/90',
            'motion-safe:transition',
          ]"
        >
          <component :is="typeIcons[link.type] ?? WebsiteIcon" class="w-6 h-6" />
          <span>{{ typeNames[link.type] }}</span>
        </a>
      </li>
    </ul>
  </Block>
</template>
