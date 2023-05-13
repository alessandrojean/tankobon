<script lang="ts" setup>
import groupBy from 'lodash.groupby'
import type { SeriesEntity } from '@/types/tankobon-series'

export interface SeriesNameProps {
  series?: SeriesEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<SeriesNameProps>(), {
  series: undefined,
  loading: false,
})

const { t } = useI18n()
const { series, loading } = toRefs(props)

const alternativeNames = computed(() => {
  return groupBy(series.value?.attributes.alternativeNames ?? [], 'language')
})

function getFirstKeyAvailable(...keys: string[]) {
  const names = keys.map(key => alternativeNames.value[key]?.[0])

  return names.find(name => name !== undefined && name.name.length > 0)
}

// https://unicode.org/iso15924/iso15924-codes.html
const possibleOriginalName = computed(() => {
  if (!series.value) {
    return undefined
  }

  const type = series.value.attributes.type

  if (type === 'MANGA' || type === 'LIGHT_NOVEL') {
    return getFirstKeyAvailable('ja-JP', 'ja-Latn-JP')
  } else if (type === 'MANHWA') {
    return getFirstKeyAvailable('ko-KR', 'ko-KP', 'ko-Latn-KR', 'ko-Latn-KP')
  } else if (type === 'MANHUA') {
    return getFirstKeyAvailable('zh-CN', 'zh-TW', 'zh-HK', 'zh-Hant-TW', 'zh-Hans-CN', 'zh-Latn-CN', 'zh-Latn-TW')
  } else {
    return series.value.attributes.alternativeNames[0]
  }
})

const originalName = computed(() => {
  if (!series.value) {
    return undefined
  }

  const { originalLanguage } = series.value.attributes

  if (!originalLanguage) {
    return possibleOriginalName.value
  }

  if (alternativeNames.value[originalLanguage]) {
    return alternativeNames.value[originalLanguage][0]
  }

  const [language] = originalLanguage.split('-')
  const firstLanguage = Object
    .keys(alternativeNames.value)
    .find(key => key.startsWith(language))

  return firstLanguage
    ? alternativeNames.value[firstLanguage][0]
    : series.value.attributes.alternativeNames[0]
})

const typeName = computed(() => {
  const type = series.value?.attributes.type
  const typeKey = type ? type.toLowerCase().replace(/_/g, '-') : 'unknown'

  return t(`series-types.${typeKey}`)
})
</script>

<template>
  <div class="flex flex-col sm:h-56 text-gray-700 sm:text-white/80 2xl:pr-52">
    <h2
      v-if="!loading"
      class="pt-3 text-gray-900 sm:text-white/95 dark:text-white/95 text-xl sm:text-2xl md:text-3xl font-display-safe font-semibold"
    >
      {{ series!.attributes.name }}
    </h2>
    <div
      v-else
      aria-hidden="true"
      class="mt-3 skeleton w-44 sm:w-72 h-8 mb-2 bg-white/50 dark:bg-white/30"
    />

    <p
      v-if="!loading && originalName"
      :lang="originalName.language"
      class="font-display-safe text-md sm:text-lg sm:text-white/80 dark:text-white/80 md:text-xl mb-2"
    >
      {{ originalName.name }}
    </p>

    <div class="flex-grow hidden sm:block" aria-hidden="true" />

    <p
      v-if="!loading && series?.attributes.type"
      class="text-sm md:text-base sm:text-white/90 dark:text-white/90"
    >
      {{ typeName }}
    </p>
    <div
      v-else-if="loading"
      class="skeleton h-6 w-40 bg-white/50 dark:bg-white/30"
      aria-hidden="true"
    />
  </div>
</template>
