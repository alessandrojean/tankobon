<template>
  <div class="bg-white min-h-screen dark:bg-gray-900">
    <div class="md:flex w-full">
      <div class="shrink-0 hidden lg:block">
        <AsideMenu
          class="sticky inset-x-0 top-0 h-screen"
          id="aside-menu"
          collapsible
        />
      </div>
      <div class="flex-1 flex flex-col relative" id="main-content">
        <!-- <DashboardNavbar
          class="sticky inset-x-0 top-0 shrink-0"
          :transparent="route.meta?.transparentNavbar && smAndLarger"
        /> -->
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
    <!-- <DashboardAsideDialog :is-open="asideDialogOpen" @close="closeAsideDialog">
      <template #footer v-if="enabled && shared">
        <div class="border-t border-gray-200 dark:border-gray-700 py-2">
          <BookOwnerBadge />
        </div>
      </template>
    </DashboardAsideDialog> -->
  </div>
</template>