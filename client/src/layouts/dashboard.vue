<script lang="ts" setup>
import { ShowAsideDialogKey } from '@/symbols';
import { breakpointsTailwind } from '@vueuse/core'

const breakpoints = useBreakpoints(breakpointsTailwind)
const smAndLarger = breakpoints.greaterOrEqual('sm')
const lgAndLarger = breakpoints.greaterOrEqual('lg')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const userId = computed(() => userStore.me!.id)
const { data: hasNoLibraries } = useUserLibrariesByUserQuery({
  userId,
  select: (libraries) => libraries.length === 0,
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
</script>

<template>
  <div class="bg-white min-h-screen dark:bg-gray-950">
    <div class="md:flex w-full">
      <div class="shrink-0 hidden lg:block">
        <AsideMenu
          class="sticky inset-x-0 top-0 h-screen"
          id="aside-menu"
          collapsible
          :is-admin="isAdmin"
        />
      </div>
      <div class="flex-1 flex flex-col relative" id="main-content">
        <Navbar
          class="sticky inset-x-0 top-0 shrink-0"
          :transparent="route.meta?.transparentNavbar && smAndLarger"
        />
        <main role="main" class="flex-1">
          <RouterView v-slot="{ Component }">
            <FadeTransition>
              <component :is="Component" class="w-full" />
            </FadeTransition>
          </RouterView>
        </main>
        <!-- <DashboardFooter class="shrink-0" /> -->
      </div>
    </div>
    <AsideDialog
      :is-open="asideDialogOpen"
      :is-admin="isAdmin"
      @close="closeAsideDialog"
    />
  </div>
</template>