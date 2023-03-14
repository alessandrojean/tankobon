<script lang="ts" setup>
const router = useRouter()
const {
  data: libraries,
  isLoading: isLoadingLibraries
} = useUserLibrariesQuery()

watch([libraries, isLoadingLibraries], ([libraries]) => {
  if (libraries && libraries.length === 0) {
    router.replace({ name: 'welcome' })
  }
})
</script>

<route lang="yaml">
meta:
  layout: dashboard
</route>

<template>
  <div>
    <h1>Authenticated</h1>
  </div>
</template>