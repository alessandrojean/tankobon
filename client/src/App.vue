<script lang="ts" setup>
const userStore = useUserStore()
const router = useRouter()

const isAuthenticated = computed(() => userStore.isAuthenticated)

watch(isAuthenticated, async (current, previous) => {
  if (!current && previous) {
    await router.push({ name: 'sign-in' })
  }
})
</script>

<template>
  <router-view v-slot="{ Component }">
    <FadeTransition>
      <component :is="Component" />
    </FadeTransition>
  </router-view>
</template>
