<script setup lang="ts">
import { XMarkIcon } from '@heroicons/vue/20/solid'
import { helpers, required, url as urlValidator } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'

export interface EntityExternalLinkFormCardProps {
  disabled?: boolean
  disabledTypes?: string[]
  index: number
  invalid?: boolean
  type: string
  types: string[]
  url: string
}

const props = withDefaults(defineProps<EntityExternalLinkFormCardProps>(), {
  disabled: false,
  disabledTypes: () => [],
  invalid: false,
})

defineEmits<{
  (e: 'update:type', type: string): void
  (e: 'update:url', url: string): void
  (e: 'click:remove'): void
}>()

const { type, url } = toRefs(props)
const { t } = useI18n()

function notNullValue(value: string) {
  return value !== 'null'
}

interface AllowedHosts {
  name: string
  hosts: string[]
}

const allowedHostsMap: Record<string, AllowedHosts> = {
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
    hosts: ['amazon.com', 'amazon.ca', 'amazon.com.br', 'amazon.co.uk', 'amazon.co.jp', 'amazon.cn'],
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
    hosts: ['mangaupdates.com']
  },
}

function allowedHost(value: string) {
  const typeAllowedHosts = allowedHostsMap[type.value]

  if (value.length === 0 || !typeAllowedHosts || !urlValidator.$validator(value, null, null)) {
    return true
  }

  try {
    const urlParsed = new URL(value)
    const host = urlParsed.hostname.toLowerCase().replace(/^www./, '')

    return typeAllowedHosts.hosts.includes(host)
  } catch (_) {
    return false
  }
}

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)
  const messageNotNull = helpers.withMessage(t('validation.not-unknown'), notNullValue)
  const messageUrl = helpers.withMessage(t('validation.url'), urlValidator)
  const messageAllowedHosts = helpers.withMessage(
    () => t('validation.allowed-hosts', { site: allowedHostsMap[type.value]?.name ?? '' }),
    allowedHost
  )

  return {
    type: { messageRequired, messageNotNull },
    url: { messageRequired, messageUrl, messageAllowedHosts },
  }
})

const v$ = useVuelidate(rules, { type, url })

defineExpose({ v$ })
</script>

<template>
  <fieldset
    :disabled="disabled"
    :class="[
      'py-4 md:py-3 px-4 rounded-xl',
      'flex items-start gap-4',
      'motion-safe:transition',
      'bg-gray-100 dark:bg-gray-900',
      {
        'ring-2 ring-red-600 dark:ring-red-500/60': invalid,
      },
    ]"
  >
    <ExternalLinkInput
      class="grow -mr-2"
      :index="index"
      :url="url"
      :type="type"
      :types="types"
      :invalid-type="v$.type.$error"
      :invalid-url="v$.url.$error"
      :errors-type="v$.type.$errors"
      :errors-url="v$.url.$errors"
      :disabled-types="disabledTypes"
      @blur:url="v$.url.$touch()"
      @blur:type="v$.type.$touch()"
      @update:url="$emit('update:url', $event)"
      @update:type="$emit('update:type', $event)"
    />

    <Button
      class="shrink-0 w-10 h-10 -mr-2 mt-px"
      kind="ghost-alt"
      size="small"
      :title="$t('common-actions.remove')"
      @click="$emit('click:remove')"
    >
      <span class="sr-only">{{ $t('common-actions.remove') }}</span>
      <XMarkIcon class="w-5 h-5" />
    </Button>
  </fieldset>
</template>
