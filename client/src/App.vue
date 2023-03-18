<script lang="ts" setup>
import { NotificateKey } from './symbols';

export type Theme = 'system' | 'dark' | 'light'
export interface Notification {
  title: string,
  body?: string,
  type?: 'success' | 'info' | 'failure' | 'warning',
}

const THEME_SYSTEM: Theme = 'system'
const THEME_DARK: Theme = 'dark'
const THEME_LIGHT: Theme = 'light'

const { t } = useI18n()
const userStore = useUserStore()
const router = useRouter()

const isAuthenticated = computed(() => userStore.isAuthenticated)
const isPreferredDark = useMediaQuery('(prefers-color-scheme: dark)')
const localTheme = useLocalStorage('theme', THEME_SYSTEM)
const { data: theme } = useUserPreferencesQuery({
  select: (preferences) => preferences.theme as Theme ?? localTheme.value,
  onError: (error) => {
    notificate({
      title: t('preferences.theme-failure'),
      body: error.message,
      type: 'failure',
    })
  }
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

const route = useRoute()
const isDashboard = computed(() => route.meta.layout === 'dashboard')
const notificationTop = computed(() => isDashboard.value ? 5 : 1)

type InnerNotification = Notification & { id: string }

const notifications = ref<InnerNotification[]>([])
const notificator = ref<HTMLDivElement | null>()
const motionOk = useMediaQuery('(prefers-reduced-motion: no-preference)')

async function addNotification(notification: Notification) {
  if (motionOk.value) {
    return await flipNotification(notification)
  } else {
    const id = new Date().getTime().toString(16)

    notifications.value.push({ ...notification, id })
    await nextTick()

    return notificator.value!.querySelector<HTMLOutputElement>(`[data-id='${id}']`)
  }
}

async function flipNotification(notification: Notification) {
  const id = new Date().getTime().toString(16)
  const last = notificator.value!.offsetHeight

  notifications.value.unshift({ ...notification, id })
  await nextTick()

  const first = notificator.value!.offsetHeight

  const invert = last - first

  const animation = notificator.value!.animate(
    [
      { transform: `translateY(${invert}px)` },
      { transform: 'translateY(0)' }
    ],
    {
      duration: 150,
      easing: 'ease-out',
    }
  )

  return notificator.value!.querySelector<HTMLOutputElement>(`[data-id='${id}']`)
}

async function notificate(notification: Notification): Promise<void> {
  const notificationElement = await addNotification(notification)
  
  if (!notificationElement) {
    return
  }

  const id = notificationElement.dataset.id

  return new Promise(async (resolve) => {
    await Promise.allSettled(
      notificationElement.getAnimations()
        .map(animation => animation.finished)
    )

    const index = notifications.value.findIndex((n) => n.id === id)
    notifications.value.splice(index, 1)
    await nextTick()
    resolve()
  })
}

provide(NotificateKey, notificate)
</script>

<template>
  <div>
    <router-view v-slot="{ Component }">
      <FadeTransition>
        <component :is="Component" />
      </FadeTransition>
    </router-view>

    <div
      :class="[
        'fixed z-50 top-[var(--top)] right-4',
        'grid justify-end content-end gap-4',
        'pointer-events-none will-change-transform',
      ]"
      :style="{ '--top': `${notificationTop}rem` }"
      ref="notificator"
    >
      <Notification
        v-for="notification in notifications"
        class="animate-notification"
        :data-id="notification.id"
        :key="notification.id"
        :title="notification.title"
        :body="notification.body"
        :type="notification.type"
      />
    </div>
  </div>
</template>
