<script setup lang="ts">
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const notificator = useNotificator()
const { t } = useI18n()

const { data: claimStatus, refetch: checkClaim } = useServerClaimStatusQuery({
  onError: async (error) => {
    await notificator.failure({
      title: t('claim-server.fetch-failure'),
      body: error.message,
    })
  }
})

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
  <div class="bg-gray-100 dark:bg-gray-900 w-full h-full min-h-screen flex items-center justify-center">
    <LoadingSpinIcon class="animate-spin w-10 h-10 text-primary-600" />
  </div>
</template>
