<script lang="ts" setup>
import { filesize } from 'filesize'
import humanizeDuration from 'humanize-duration'
import type { Metric } from '@/types/tankobon-metrics'

export interface MetricCardProps {
  metric: Metric,
  unit?: string,
  title: string,
}

const props = withDefaults(defineProps<MetricCardProps>(), {
  unit: undefined
})
const { metric, unit } = toRefs(props)

const { locale } = useI18n()

const value = computed(() => metric.value.measurements[0].value)
const baseUnit = computed(() => unit.value ?? metric.value.baseUnit)
const intlFormatters = computed(() => ({
  percent: new Intl.NumberFormat(locale.value, {
    style: 'percent',
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  })
}))

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
  switch (baseUnit.value) {
    case 'bytes': {
      const [formatted, unit] = filesize(value.value, {
        locale: locale.value,
        output: 'array',
      }) as string[]

      return { formatted, unit }
    }
    case 'percent': {
      const formatted = intlFormatters.value.percent.format(value.value)
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
    default: return null
  }
})
</script>

<template>
  <div
    :class="[
      'bg-gray-100 dark:bg-gray-800 rounded-lg p-4'
    ]"
  >
    <p class="font-display text-sm font-medium text-gray-800 dark:text-gray-200">
      {{ title }}
    </p>
    <p  
      v-if="formattedValue"
      class="text-3xl font-medium mt-2 text-primary-600 dark:text-primary-500"
    >
      {{ formattedValue.formatted }}
      <span class="text-sm text-gray-600 dark:text-gray-400">
        {{ formattedValue.unit }}
      </span>
    </p>
  </div>
</template>
