<script lang="ts" setup>
import { breakpointsTailwind } from '@vueuse/core'
import { SetDialogOpenKey, ShowAsideDialogKey, ShowSearchPaletteKey } from '@/symbols'

const breakpoints = useBreakpoints(breakpointsTailwind)
const smAndLarger = breakpoints.greaterOrEqual('sm')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const userId = computed(() => userStore.me?.id)
const { data: hasNoLibraries } = useUserLibrariesByUserQuery({
  enabled: computed(() => userStore.isAuthenticated),
  userId: userId as ComputedRef<string>,
  select: libraries => libraries.data.length === 0,
})

whenever(hasNoLibraries, async () => await router.replace({ name: 'welcome' }))

const asideDialogOpen = ref(false)

function openAsideDialog() {
  asideDialogOpen.value = true
}

function closeAsideDialog() {
  asideDialogOpen.value = false
}

provide(ShowAsideDialogKey, openAsideDialog)

const searchPaletteOpen = ref(false)
const dialogOpen = ref(false)

provide(ShowSearchPaletteKey, () => {
  searchPaletteOpen.value = true
})

provide(SetDialogOpenKey, (open) => {
  dialogOpen.value = open
})

const keys = useMagicKeys({
  passive: false,
  onEventFired(e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k' && e.type === 'keydown' && !dialogOpen.value) {
      e.preventDefault()
    }
  },
})
const ctrlK = keys['Ctrl+K']
const metaK = keys['Meta+K']

whenever(logicOr(ctrlK, metaK), () => {
  if (!dialogOpen.value) {
    searchPaletteOpen.value = true
  }
})
</script>

<template>
  <div class="bg-white min-h-screen dark:bg-gray-950">
    <div class="md:flex w-full">
      <div class="shrink-0 hidden lg:block">
        <AsideMenu
          id="aside-menu"
          class="sticky inset-x-0 top-0 h-screen"
          collapsible
          :is-admin="isAdmin"
        />
      </div>
      <div id="main-content" class="flex-1 flex flex-col relative">
        <Navbar
          class="sticky inset-x-0 top-0 shrink-0"
          :transparent="route.meta?.transparentNavbar && smAndLarger"
        />
        <main role="main" class="flex-1">
          <RouterView v-slot="{ Component }">
            <FadeTransition>
              <Component :is="Component" class="w-full" />
            </FadeTransition>
          </RouterView>
        </main>
      </div>
    </div>

    <AsideDialog
      :is-open="asideDialogOpen"
      :is-admin="isAdmin"
      @close="closeAsideDialog"
    />

    <SearchPalette
      v-model:open="searchPaletteOpen"
    />
  </div>
</template>
