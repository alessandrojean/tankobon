<script lang="ts" setup>
export type Theme = 'system' | 'dark' | 'light'

const THEME_SYSTEM: Theme = 'system'
const THEME_DARK: Theme = 'dark'
const THEME_LIGHT: Theme = 'light'

const userStore = useUserStore()
const router = useRouter()

const isAuthenticated = computed(() => userStore.isAuthenticated)
const isPreferredDark = useMediaQuery('(prefers-color-scheme: dark)')
const { data: theme } = useUserPreferencesQuery({
  select: (preferences) => preferences.theme as Theme ?? THEME_SYSTEM
})

watch([theme, isPreferredDark], ([theme, isPreferredDark]) => {
  if ((theme === THEME_SYSTEM && isPreferredDark) || theme === THEME_DARK) {
    document.documentElement.classList.add(THEME_DARK)
  } else if ((theme === THEME_SYSTEM && !isPreferredDark) || theme === THEME_LIGHT) {
    document.documentElement.classList.remove(THEME_DARK)
  }
})

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
