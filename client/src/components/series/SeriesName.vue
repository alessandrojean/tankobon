<script lang="ts" setup>
import type { SeriesEntity } from '@/types/tankobon-series'
import { getOriginalName } from '@/services/tankobon-series'

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

const originalName = computed(() => getOriginalName(series.value))

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
