<script lang="ts" setup>
const { t } = useI18n()

useHead({ title: () => t('dashboard.title') })
</script>

<route lang="yaml">
meta:
  layout: dashboard
</route>

<template>
  <div>
    <Header title="Dashboard" />
  </div>
</template>
