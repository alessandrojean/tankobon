<script lang="ts" setup>
import { filesize } from 'filesize'
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
const percentFormatter = computed(() => {
  return new Intl.NumberFormat(locale.value, {
    style: 'percent',
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  })
})

const formattedValue = computed<string[]>(() => {
  switch (baseUnit.value) {
    case 'bytes':
      return filesize(value.value, {
        locale: locale.value,
        output: 'array',
      }) as string[]
    case 'percent':
      const formatted = percentFormatter.value.format(value.value)
      return [formatted.replace('%', '').trim(), '%']
    default: return []
  }
})
</script>

<template>
  <div
    :class="[
      'bg-gray-100 rounded-lg p-4'
    ]"
  >
    <p class="font-display text-sm font-medium text-gray-800">
      {{ title }}
    </p>
    <p class="text-3xl font-medium mt-2 text-primary-600">
      {{ formattedValue[0] }}
      <span class="text-sm text-gray-600">
        {{ formattedValue[1] }}
      </span>
    </p>
  </div>
</template>
