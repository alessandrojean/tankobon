<script lang="ts" setup>
const { t } = useI18n()

useHead({ title: () => t('statistics.title') })
</script>

<route lang="yaml">
meta:
  layout: dashboard
</route>

<template>
  <div>
    <Header :title="$t('statistics.title')" />
  </div>
</template>
