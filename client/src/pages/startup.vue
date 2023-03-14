<script setup lang="ts">
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const { data: claimStatus, refetch: checkClaim } = useServerClaimStatusQuery()

onBeforeMount(async () => {
  try {
    await checkClaim()
    await userStore.checkSession()
    
    if (route.query.redirect) {
      await router.replace({ path: route.query.redirect.toString() })
    } else {
      await router.replace({ name: 'index' })
    }
  } catch (e) {
    if (claimStatus.value?.isClaimed) {
      router.replace({
        name: 'sign-in',
        query: { redirect: route.query.redirect }
      })
    } else {
      router.replace({ name: 'claim-server' })
    }
  }
})
</script>

<template>
  <div class="bg-gray-100 dark:bg-gray-900 w-full h-full flex items-center justify-center">
    <LoadingSpinIcon class="animate-spin w-10 h-10" />
  </div>
</template>
