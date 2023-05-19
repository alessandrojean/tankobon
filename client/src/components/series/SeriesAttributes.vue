<script lang="ts" setup>
import { SeriesEntity } from '@/types/tankobon-series'
import { getLanguageName } from '@/utils/language'

export interface SeriesAttributesProps {
  series?: SeriesEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<SeriesAttributesProps>(), {
  series: undefined,
  loading: false,
})

const { series, loading } = toRefs(props)
const { t, locale } = useI18n()


const metadata = computed(() => {
  const attributes = series.value?.attributes
  const typeKey = attributes?.type 
    ? attributes.type.toLowerCase().replace(/_/g, '-')
    : 'unknown'

  return [
    {
      title: t('original-language.label'),
      value: getLanguageName({
        language: attributes?.originalLanguage,
        locale: locale.value,
        romanizedLabel: t('original-language.romanized'),
        unknownLabel: t('original-language.unknown'),
      }),
    },
    {
      title: t('series.type'),
      value: t(`series-types.${typeKey}`),
    },
    {
      title: t('series.last-number'),
      value: attributes?.lastNumber?.length ? attributes.lastNumber : t('series.last-number-unknown'),
    },
  ]
})
</script>

<template>
  <Block as="dl">
    <div class="space-y-5">
      <template v-for="(mt, i) in metadata">
        <div v-if="mt.value || loading" :key="i">
          <dt
            v-if="!loading"
            class="text-sm font-medium text-gray-950 dark:text-gray-100"
          >
            {{ mt.title }}
          </dt>
          <div v-else aria-hidden="true" class="skeleton w-16 h-5" />

          <dd
            v-if="!loading"
            class="mt-1 text-sm text-gray-700 dark:text-gray-300/90 inline-flex items-center"
          >
            <span>{{ mt.value }}</span>
          </dd>
          <div v-else aria-hidden="true" class="mt-1 skeleton w-32 h-6" />
        </div>
      </template>
    </div>
  </Block>
</template>
