<script lang="ts" setup>
import { ShowToastKey } from './symbols'

export type Theme = 'system' | 'dark' | 'light'
export interface Toast {
  title: string
  body?: string
  type?: 'success' | 'info' | 'failure' | 'warning'
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
  select: preferences => preferences.theme as Theme ?? localTheme.value,
  onError: (error) => {
    showToast({
      title: t('preferences.theme-failure'),
      body: error.message,
      type: 'failure',
    })
  },
})

watch([theme, isPreferredDark], ([theme, isPreferredDark]) => {
  if ((theme === THEME_SYSTEM && isPreferredDark) || theme === THEME_DARK)
    document.documentElement.classList.add(THEME_DARK)
  else if ((theme === THEME_SYSTEM && !isPreferredDark) || theme === THEME_LIGHT)
    document.documentElement.classList.remove(THEME_DARK)
})

watch(isAuthenticated, async (current, previous) => {
  if (!current && previous)
    await router.push({ name: 'sign-in' })
})

const route = useRoute()
const isDashboard = computed(() => route.meta.layout === 'dashboard')
const toastTop = computed(() => isDashboard.value ? 5 : 1)

type InnerToast = Toast & { id: string }

const toasts = ref<InnerToast[]>([])
const toaster = ref<HTMLDivElement | null>()
const motionOk = useMediaQuery('(prefers-reduced-motion: no-preference)')

async function addToast(toast: Toast) {
  if (motionOk.value) {
    return await flipToast(toast)
  }
  else {
    const id = new Date().getTime().toString(16)

    toasts.value.push({ ...toast, id })
    await nextTick()

    return toaster.value!.querySelector<HTMLOutputElement>(`[data-id='${id}']`)
  }
}

async function flipToast(toast: Toast) {
  const id = new Date().getTime().toString(16)
  const last = toaster.value!.offsetHeight

  toasts.value.unshift({ ...toast, id })
  await nextTick()

  const first = toaster.value!.offsetHeight

  const invert = last - first

  if (toasts.value.length > 1) {
    toaster.value!.animate(
      [
        { transform: `translateY(${invert}px)` },
        { transform: 'translateY(0)' },
      ],
      {
        duration: 150,
        easing: 'ease-out',
      },
    )
  }

  return toaster.value!.querySelector<HTMLOutputElement>(`[data-id='${id}']`)
}

async function showToast(toast: Toast): Promise<void> {
  const toastElement = await addToast(toast)

  if (!toastElement)
    return

  const id = toastElement.dataset.id

  return new Promise((resolve) => {
    Promise
      .allSettled(
        toastElement.getAnimations()
          .map(animation => animation.finished),
      )
      .then(() => {
        const index = toasts.value.findIndex(n => n.id === id)
        toasts.value.splice(index, 1)
        nextTick(() => resolve())
      })
  })
}

provide(ShowToastKey, showToast)
</script>

<template>
  <div>
    <RouterView v-slot="{ Component }">
      <FadeTransition>
        <Component :is="Component" />
      </FadeTransition>
    </RouterView>

    <div
      ref="toaster"
      class="fixed z-50 top-[--top] right-4 grid justify-items-end justify-center gap-4 pointer-events-none will-change-transform"
      :style="{ '--top': `${toastTop}rem` }"
    >
      <Toast
        v-for="toast in toasts"
        :key="toast.id"
        class="animate-toast"
        :data-id="toast.id"
        :title="toast.title"
        :body="toast.body"
        :type="toast.type"
      />
    </div>
  </div>
</template>
