<script lang="ts" setup>
import { PublisherAttributes, PublisherEntity } from '@/types/tankobon-publisher'

export interface SeriesAttributesProps {
  publisher?: PublisherEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<SeriesAttributesProps>(), {
  publisher: undefined,
  loading: false,
})

const { publisher, loading } = toRefs(props)
const { t, locale } = useI18n()

const regionNames = computed(() => new Intl.DisplayNames(locale.value, {
  type: 'region',
  style: 'long',
}))

function getCompanyStatus(attributes: PublisherAttributes | undefined) {
  if (attributes?.foundingYear && attributes?.dissolutionYear) {
    return t('publishers.status-closed')
  } else if (attributes?.foundingYear) {
    return t('publishers.status-active')
  } else {
    return t('publishers.status-unknown')
  }
}

const metadata = computed(() => {
  const attributes = publisher.value?.attributes

  return [
    {
      title: t('common-fields.location'),
      value: attributes?.location
        ? regionNames.value.of(attributes?.location)
        : t('location.unknown'),
    },
    {
      title: t('publishers.company-status'),
      value: getCompanyStatus(attributes),
    },
    {
      title: t('common-fields.founding-year'),
      value: attributes?.foundingYear
        ? String(attributes?.foundingYear)
        : t('publishers.founding-unknown')
    },
    {
      title: t('common-fields.dissolution-year'),
      value: attributes?.dissolutionYear
        ? String(attributes?.dissolutionYear)
        : null
    }
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
