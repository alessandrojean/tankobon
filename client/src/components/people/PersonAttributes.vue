<script lang="ts" setup>
import { differenceInYears, parseISO, startOfDay } from 'date-fns'
import type { PersonEntity } from '@/types/tankobon-person'

export interface PersonAttributesProps {
  person?: PersonEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<PersonAttributesProps>(), {
  person: undefined,
  loading: false,
})

const { person, loading } = toRefs(props)
const { t, d, locale } = useI18n()

function formatDate(date: string | null | undefined, format = 'short') {
  if (typeof date === 'string' && date.length > 0) {
    return d(
      startOfDay(parseISO(date)),
      format,
    )
  }

  return t('date.unknown')
}

const regionNames = computed(() => new Intl.DisplayNames(locale.value, {
  type: 'region',
  style: 'long',
}))

const metadata = computed(() => {
  const attributes = person.value?.attributes

  return [
    {
      title: t('common-fields.nationality'),
      value: attributes?.nationality
        ? regionNames.value.of(attributes?.nationality)
        : t('nationality.unknown'),
    },
    {
      title: t('common-fields.born-at'),
      value: attributes?.bornAt,
      time: true,
    },
    {
      title: t('common-fields.died-at'),
      value: attributes?.diedAt,
      years: (attributes?.bornAt && attributes?.diedAt)
        ? differenceInYears(parseISO(attributes.diedAt), parseISO(attributes.bornAt))
        : null,
      time: true,
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
            <time v-if="mt.time" :datetime="mt.value ?? undefined">
              {{
                mt.years
                  ? $t('people.died-at-attribute', {
                    date: formatDate(mt.value!),
                    years: $n(mt.years, 'unit', { unit: 'year', unitDisplay: 'long' }),
                  })
                  : formatDate(mt.value!)
              }}
            </time>
            <span v-else>{{ mt.value }}</span>
          </dd>
          <div v-else aria-hidden="true" class="mt-1 skeleton w-32 h-6" />
        </div>
      </template>
    </div>
  </Block>
</template>
