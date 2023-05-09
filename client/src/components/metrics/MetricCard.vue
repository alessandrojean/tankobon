<script lang="ts" setup>
import type { Unit } from '@/components/StatisticCard.vue'
import type { Metric } from '@/types/tankobon-metrics'

export interface MetricCardProps {
  measurement?: number
  metric: Metric
  unit?: string
  title: string
}

const props = withDefaults(defineProps<MetricCardProps>(), {
  measurement: 0,
  unit: undefined,
})
const { metric, unit, measurement } = toRefs(props)

const value = computed(() => metric.value.measurements[measurement.value].value)
const baseUnit = computed(() => (unit.value ?? metric.value.baseUnit) as Unit)
</script>

<template>
  <StatisticCard
    :value="value"
    :unit="baseUnit"
    :title="title"
  />
</template>
