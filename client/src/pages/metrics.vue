<script lang="ts" setup>
const { t } = useI18n()
const notificator = useNotificator()
const { data: metrics } = useMetricsQuery({
  metrics: [
    'disk.free',
    'disk.total',
    'tankobon.books',
    'tankobon.libraries',
    'tankobon.users',
  ],
  onError: async (error) => {
    await notificator.failure({
      title: t('metrics.fetch-failure'),
      body: error.message,
    })
  }
})

const { data: cpuMetrics } = useMetricsQuery({
  metrics: ['system.cpu.usage'],
  refetchInterval: 5 * 1_000,
  onError: async (error) => {
    await notificator.failure({
      title: t('metrics.fetch-failure'),
      body: error.message,
    })
  }
})
</script>

<route lang="yaml">
  meta:
    layout: dashboard
    isAdminOnly: true
</route>

<template>
  <div>
    <Header
      :title="$t('metrics.header')"
    />

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <div
        v-if="metrics && cpuMetrics"
        class="grid grid-cols-4 gap-6"
      >
      <MetricCard 
          :title="$t('metrics.cpu-usage')"
          :metric="cpuMetrics['system.cpu.usage']"
          unit="percent"
        />

        <MetricCard 
          :title="$t('metrics.free-disk-space')"
          :metric="metrics['disk.free']"
        />

        <MetricCard 
          :title="$t('metrics.total-disk-space')"
          :metric="metrics['disk.total']"
        />
      </div>
    </div>
  </div>
</template>