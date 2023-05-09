<script lang="ts" setup>
import { filesize } from 'filesize'
import humanizeDuration from 'humanize-duration'

export type Unit = 'bytes' | 'percent' | 'seconds' | 'count'

export interface StatisticCard {
  value: number
  unit?: Unit
  title: string
}

const props = withDefaults(defineProps<StatisticCard>(), {
  unit: 'count',
})

const { value, unit } = toRefs(props)
const { locale, n } = useI18n()

const shortEnglishHumanizer = humanizeDuration.humanizer({
  language: 'shortEn',
  languages: {
    shortEn: {
      y: () => 'y',
      mo: () => 'mo',
      w: () => 'w',
      d: () => 'd',
      h: () => 'h',
      m: () => 'm',
      s: () => 's',
      ms: () => 'ms',
    },
  },
})

const formattedValue = computed(() => {
  switch (unit.value) {
    case 'bytes': {
      const [formatted, unit] = filesize(value.value, {
        locale: locale.value,
        output: 'array',
      }) as string[]

      return { formatted, unit }
    }
    case 'percent': {
      const formatted = n(value.value, 'percent')
      return { formatted: formatted.replace('%', '').trim(), unit: '%' }
    }
    case 'seconds': {
      const formattedString = shortEnglishHumanizer(value.value * 1_000, {
        largest: 1,
        round: true,
      })
      const [formatted, unit] = formattedString.split(' ')

      return { formatted, unit }
    }
    case 'count': {
      const formatted = n(value.value, 'integer')

      return { formatted }
    }
    default: return null
  }
})
</script>

<template>
  <Block as="div">
    <p class="text-sm font-medium text-gray-800 dark:text-gray-200">
      {{ title }}
    </p>
    <p
      v-if="formattedValue"
      class="text-3xl font-medium mt-2 text-primary-600 dark:text-primary-500"
    >
      {{ formattedValue.formatted }}
      <span
        v-if="formattedValue.unit"
        class="text-sm text-gray-600 dark:text-gray-400"
      >
        {{ formattedValue.unit }}
      </span>
    </p>
  </Block>
</template>
