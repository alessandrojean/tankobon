<script lang="ts" setup>
import type { ErrorObject } from '@vuelidate/core'
import Button from '@/components/form/Button.vue'
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
import { allowedHostsMap } from '@/utils/links'

export interface TextInputProps {
  disabledTypes?: string[]
  errorsType?: ErrorObject[]
  errorsUrl?: ErrorObject[]
  index: number
  invalidType?: boolean
  invalidUrl?: boolean
  type: string
  types: string[]
  url: string
  placeholder?: string
}

const props = withDefaults(defineProps<TextInputProps>(), {
  disabledTypes: () => [],
  errorsType: undefined,
  errorsUrl: undefined,
  invalidType: false,
  invalidUrl: false,
})

const emit = defineEmits<{
  (e: 'update:url', url: string): void
  (e: 'update:type', type: string): void
  (e: 'blur:url'): void
  (e: 'blur:type'): void
}>()

const { t } = useI18n()
const { errorsType, errorsUrl, disabledTypes, types, type, url } = toRefs(props)

const errorMessage = computed(() => {
  const errorType = unref(errorsType.value?.[0]?.$message)
  const errorUrl = unref(errorsUrl.value?.[0]?.$message)

  if (errorType === errorUrl) {
    return errorUrl
  } else if (errorType && errorType.length > 0) {
    return errorType
  } else if (errorUrl && errorUrl.length > 0) {
    return errorUrl
  } else {
    return null
  }
})

function handleUrlChange(newUrl: string) {
  emit('update:url', newUrl)
  emit('blur:url')
  emit('blur:type')
}

function handleTypeChange(newType: string) {
  emit('update:type', newType)
  emit('blur:type')
}

function blurBothFields() {
  emit('blur:url')
  emit('blur:type')
}

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
}

const disabledIndexes = computed(() => {
  return types.value
    .map((t, i) => disabledTypes.value.includes(t) && t !== type.value ? i : -1)
    .filter(i => i !== -1)
})

async function handleExternalLinkChange(type: string, url: string) {
  await nextTick()
  handleTypeChange(type)

  await nextTick()
  handleUrlChange(url)
}

async function detectTypeFromUrl(event: ClipboardEvent) {
  const pastedUrl = event.clipboardData?.getData('text/plain')

  if (!pastedUrl) {
    return
  }

  let parsedUrl: URL

  try {
    parsedUrl = new URL(pastedUrl)
  } catch (_) {
    return
  }

  const host = parsedUrl.hostname.toLowerCase().replace(/^www./, '')
  const [detectedType] = Object.entries(allowedHostsMap)
    .find(([_, value]) => value.hosts.includes(host)) ?? []

  if (!detectedType || !types.value.includes(detectedType)) {
    const freeType = ['website', 'store'].find(t => types.value.includes(t))

    if (!freeType) {
      return
    }

    await handleExternalLinkChange(freeType, pastedUrl)
    return
  }

  await handleExternalLinkChange(detectedType, pastedUrl)
}
</script>

<template>
  <fieldset class="relative disabled:opacity-50">
    <label
      :for="`name-input-${index}`"
      class="sr-only"
    >
      {{ $t('external-links.label-url', [index + 1]) }}
    </label>
    <input
      type="url"
      :id="`url-input-${index}`"
      :class="[
        'peer w-full bg-white dark:bg-gray-950 shadow-sm rounded-md',
        'dark:text-gray-200 focus:ring focus:ring-opacity-50',
        'motion-safe:transition-shadow placeholder:text-gray-500',
        'border focus:outline-none pl-[3.6rem] h-10',
        invalidUrl || invalidType
          ? 'border-red-500 dark:border-red-500/95 focus:border-red-500 dark:focus:border-red-500/95 focus:ring-red-200 dark:focus:ring-red-200/30'
          : 'border-gray-300 dark:border-gray-800 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30',
      ]"
      :placeholder="placeholder"
      :value="url"
      @paste="detectTypeFromUrl"
      @blur="blurBothFields"
      @input="handleUrlChange(($event.target! as HTMLInputElement).value)"
    >
    <div
      :class="[
        'absolute h-7 flex items-center justify-center',
        'motion-safe:transition-colors left-1.5 top-0',
        'border-r border-gray-300 dark:border-gray-800 my-1.5 pr-1.5',
      ]"
    >
      <BasicListbox
        :check-icon="false"
        :label-text="$t('external-links.label-type', [index + 1])"
        :model-value="type === '' ? 'null' : type"
        :options="types"
        :option-text="(type) => typeNames[type ?? 'null'] ?? $t('external-links.type-unknown')"
        :option-value="l => l"
        :disabled-options="[0, ...disabledIndexes]"
        @update:model-value="handleTypeChange"
      >
        <template #listbox-button>
          <ListboxButton
            :as="Button"
            class="w-9 h-9 !p-0"
            kind="ghost-alt"
            size="mini"
            :title="$t('external-links.label-type', [index + 1])"
          >
            <span class="sr-only">
              {{ $t('external-links.label-type', [index + 1]) }}
            </span>
            <component :is="typeIcons[type] ?? UnknownWebsiteIcon" class="w-6 h-6" />
          </ListboxButton>
        </template>
        <template #option="{ option }: { option: string }">
          <div class="flex items-center gap-3 w-full">
            <component :is="typeIcons[option] ?? UnknownWebsiteIcon" class="-ml-0.5 shrink-0 w-6 h-6" />
            <div class="grow">
              {{ typeNames[option ?? 'null'] ?? $t('external-links.type-unknown') }}
            </div>
          </div>
        </template>
      </BasicListbox>
    </div>

    <p
      v-if="(invalidType || invalidUrl) && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1"
    >
      {{ errorMessage }}
    </p>
  </fieldset>
</template>
